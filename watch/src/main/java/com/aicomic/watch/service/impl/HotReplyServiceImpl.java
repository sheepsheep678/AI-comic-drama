package com.aicomic.watch.service.impl;

import com.aicomic.common.autoconfigure.redisson.annotations.Lock;
import com.aicomic.common.autoconfigure.redisson.enums.LockStrategy;
import com.aicomic.watch.constants.HotReplyConstants;
import com.aicomic.watch.constants.RedisConstants;
import com.aicomic.watch.domain.dto.HotReplyCandidate;
import com.aicomic.watch.domain.po.InteractionQuestion;
import com.aicomic.watch.domain.po.InteractionReply;
import com.aicomic.watch.mapper.InteractionQuestionMapper;
import com.aicomic.watch.mapper.InteractionReplyMapper;
import com.aicomic.watch.service.IHotReplyService;
import com.aicomic.watch.utils.HotReplyScoreCalculator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 回答热度 Top1 与 Redis 排行榜实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotReplyServiceImpl implements IHotReplyService {

    /**
     * 原子领取脏集合：SMOVE dirty -> processing
     */
    private static final DefaultRedisScript<List> CLAIM_SCRIPT = new DefaultRedisScript<>();

    static {
        CLAIM_SCRIPT.setLocation(new ClassPathResource("lua/claim_hot_dirty.lua"));
        CLAIM_SCRIPT.setResultType(List.class);
    }

    private final StringRedisTemplate redisTemplate;
    private final InteractionReplyMapper replyMapper;
    private final InteractionQuestionMapper questionMapper;

    @Override
    public Map<Long, List<HotReplyCandidate>> loadHotReplies(List<InteractionQuestion> questions, int limit) {
        if (questions == null || questions.isEmpty() || limit <= 0) {
            return Collections.emptyMap();
        }
        Map<Long, List<HotReplyCandidate>> result = new HashMap<>(questions.size());
        Map<Long, List<Long>> idsByQuestion = new HashMap<>(questions.size());
        Set<Long> allReplyIds = new HashSet<>();
        Set<Long> fallbackQuestionIds = new HashSet<>();

        // 1.以 DB 字段为准：NULL=立即失效走实时兜底，0=已计算但无回答；
        //    只有已计算出 Top1 的问题才优先用 Redis 排行榜补齐 TopN
        for (InteractionQuestion question : questions) {
            Long questionId = question.getId();
            if (questionId == null) {
                continue;
            }
            Long hotReplyId = question.getHotReplyId();
            if (hotReplyId == null) {
                fallbackQuestionIds.add(questionId);
                continue;
            }
            if (hotReplyId == 0L) {
                result.put(questionId, Collections.emptyList());
                continue;
            }
            List<Long> cachedIds = readRankFromRedis(questionId, limit);
            if (!cachedIds.isEmpty()) {
                idsByQuestion.put(questionId, cachedIds);
                allReplyIds.addAll(cachedIds);
            } else {
                idsByQuestion.put(questionId, Collections.singletonList(hotReplyId));
                allReplyIds.add(hotReplyId);
            }
        }

        // 2.批量加载回答详情，过滤缺失/隐藏
        Map<Long, InteractionReply> replyMap = loadReplies(allReplyIds);
        for (Map.Entry<Long, List<Long>> entry : idsByQuestion.entrySet()) {
            List<HotReplyCandidate> candidates = new ArrayList<>(entry.getValue().size());
            boolean stale = false;
            for (Long replyId : entry.getValue()) {
                InteractionReply reply = replyMap.get(replyId);
                if (reply == null || Boolean.TRUE.equals(reply.getHidden())) {
                    stale = true;
                    continue;
                }
                candidates.add(HotReplyCandidate.from(reply));
            }
            if (stale) {
                // 缓存中的回答已被删除或隐藏，实时查库重建
                fallbackQuestionIds.add(entry.getKey());
            } else {
                result.put(entry.getKey(), candidates);
            }
        }

        // 3.未计算或缓存失效的问题实时查库兜底，并打脏标记等待任务落库
        if (!fallbackQuestionIds.isEmpty()) {
            result.putAll(loadFromDatabase(fallbackQuestionIds, limit));
            markDirty(fallbackQuestionIds);
        }

        for (InteractionQuestion question : questions) {
            result.putIfAbsent(question.getId(), Collections.emptyList());
        }
        return result;
    }

    @Override
    public boolean markDirty(Collection<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return true;
        }
        String[] members = questionIds.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .toArray(String[]::new);
        if (members.length == 0) {
            return true;
        }
        try {
            redisTemplate.opsForSet().add(RedisConstants.HOT_REPLY_DIRTY_KEY, members);
            return true;
        } catch (Exception e) {
            log.warn("标记热度回答待重算失败：{}", questionIds, e);
            return false;
        }
    }

    @Override
    public void invalidate(Collection<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return;
        }
        List<Long> ids = questionIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        try {
            questionMapper.invalidateHotReplyIds(ids);
        } catch (Exception e) {
            log.warn("将问题热度回答字段置空失败：{}", ids, e);
        }
    }

    @Override
    @Lock(name = HotReplyConstants.REFRESH_LOCK, lockStrategy = LockStrategy.SKIP_FAST)
    public void refreshDirty() {
        // 1.恢复上次异常退出时停留在 processing 的问题
        recoverProcessing();
        // 2.原子领取一批待重算问题
        Set<String> claimed = claimDirty();
        if (claimed == null || claimed.isEmpty()) {
            return;
        }
        List<Long> questionIds = claimed.stream()
                .map(this::parseLong)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            removeProcessing(claimed);
            return;
        }
        // 3.分批聚合查询、更新 DB Top1、重建 Redis 排行榜
        for (List<Long> chunk : partition(questionIds, HotReplyConstants.DIRTY_BATCH_SIZE)) {
            refreshQuestions(chunk);
            removeProcessing(chunk);
        }
    }

    @Override
    @Lock(name = HotReplyConstants.BACKFILL_LOCK, lockStrategy = LockStrategy.SKIP_FAST)
    public Long backfill(Long startId, int pageSize) {
        long cursor = startId == null || startId < 0 ? 0L : startId;
        int size = pageSize <= 0 ? 200 : Math.min(pageSize, 1000);
        while (true) {
            List<InteractionQuestion> page = questionMapper.selectList(
                    new LambdaQueryWrapper<InteractionQuestion>()
                            .gt(InteractionQuestion::getId, cursor)
                            .orderByAsc(InteractionQuestion::getId)
                            .last("LIMIT " + size));
            if (page == null || page.isEmpty()) {
                break;
            }
            List<Long> ids = page.stream()
                    .map(InteractionQuestion::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                refreshQuestions(ids);
                cursor = ids.get(ids.size() - 1);
            }
            if (page.size() < size) {
                break;
            }
        }
        return cursor;
    }

    @Override
    public void cleanup(Long questionId) {
        if (questionId == null) {
            return;
        }
        try {
            redisTemplate.opsForSet().remove(RedisConstants.HOT_REPLY_DIRTY_KEY, String.valueOf(questionId));
            redisTemplate.opsForSet().remove(RedisConstants.HOT_REPLY_PROCESSING_KEY, String.valueOf(questionId));
            redisTemplate.delete(RedisConstants.HOT_REPLY_RANK_KEY_PREFIX + questionId);
        } catch (Exception e) {
            log.warn("清理问题{}的热度缓存失败", questionId, e);
        }
    }

    /**
     * 数据库聚合查询候选集，按统一热度规则取 TopN
     */
    private Map<Long, List<HotReplyCandidate>> loadFromDatabase(Collection<Long> questionIds, int limit) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<HotReplyCandidate> candidates;
        try {
            candidates = replyMapper.selectHotCandidates(
                    questionIds, HotReplyConstants.CANDIDATE_LIMIT_PER_QUESTION);
        } catch (Exception e) {
            log.warn("查询问题热度候选失败：{}", questionIds, e);
            return Collections.emptyMap();
        }
        Map<Long, List<HotReplyCandidate>> grouped = groupByQuestion(candidates);
        LocalDateTime now = LocalDateTime.now();
        Map<Long, List<HotReplyCandidate>> result = new HashMap<>(questionIds.size());
        for (Long questionId : questionIds) {
            result.put(questionId, HotReplyScoreCalculator.topN(
                    grouped.getOrDefault(questionId, Collections.emptyList()), limit, now));
        }
        return result;
    }

    /**
     * 刷新一批问题：DB Top1 + Redis 排行榜
     */
    private void refreshQuestions(Collection<Long> questionIds) {
        List<HotReplyCandidate> candidates = replyMapper.selectHotCandidates(
                questionIds, HotReplyConstants.CANDIDATE_LIMIT_PER_QUESTION);
        Map<Long, List<HotReplyCandidate>> grouped = groupByQuestion(candidates);
        LocalDateTime now = LocalDateTime.now();
        for (Long questionId : questionIds) {
            List<HotReplyCandidate> top = HotReplyScoreCalculator.topN(
                    grouped.getOrDefault(questionId, Collections.emptyList()),
                    HotReplyConstants.LEADERBOARD_SIZE, now);
            Long topReplyId = top.isEmpty() ? 0L : top.get(0).getReplyId();
            InteractionQuestion update = new InteractionQuestion();
            update.setId(questionId);
            update.setHotReplyId(topReplyId);
            questionMapper.updateById(update);
            writeRankCache(questionId, top, now);
        }
    }

    private void writeRankCache(Long questionId, List<HotReplyCandidate> top, LocalDateTime now) {
        String key = RedisConstants.HOT_REPLY_RANK_KEY_PREFIX + questionId;
        try {
            redisTemplate.delete(key);
            if (top == null || top.isEmpty()) {
                return;
            }
            Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>(top.size());
            for (HotReplyCandidate candidate : top) {
                tuples.add(ZSetOperations.TypedTuple.of(
                        String.valueOf(candidate.getReplyId()),
                        HotReplyScoreCalculator.score(candidate, now)));
            }
            redisTemplate.opsForZSet().add(key, tuples);
            redisTemplate.expire(key, HotReplyConstants.RANK_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            // 排行榜只是缓存，失败不影响 DB Top1
            log.warn("写入问题{}的热度排行榜失败", questionId, e);
        }
    }

    private List<Long> readRankFromRedis(Long questionId, int limit) {
        try {
            Set<String> members = redisTemplate.opsForZSet()
                    .reverseRange(RedisConstants.HOT_REPLY_RANK_KEY_PREFIX + questionId, 0, limit - 1L);
            if (members == null || members.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> ids = new ArrayList<>(members.size());
            for (String member : members) {
                Long id = parseLong(member);
                if (id != null && id > 0) {
                    ids.add(id);
                }
            }
            return ids;
        } catch (Exception e) {
            log.warn("读取问题{}的热度排行榜失败，降级查库", questionId, e);
            return Collections.emptyList();
        }
    }

    private Map<Long, InteractionReply> loadReplies(Collection<Long> replyIds) {
        if (replyIds == null || replyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<InteractionReply> replies = replyMapper.selectBatchIds(replyIds);
        if (replies == null || replies.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, InteractionReply> result = new HashMap<>(replies.size());
        for (InteractionReply reply : replies) {
            if (reply != null && reply.getId() != null) {
                result.put(reply.getId(), reply);
            }
        }
        return result;
    }

    private Map<Long, List<HotReplyCandidate>> groupByQuestion(List<HotReplyCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<HotReplyCandidate>> grouped = new HashMap<>();
        for (HotReplyCandidate candidate : candidates) {
            if (candidate == null || candidate.getQuestionId() == null) {
                continue;
            }
            grouped.computeIfAbsent(candidate.getQuestionId(), key -> new ArrayList<>())
                    .add(candidate);
        }
        return grouped;
    }

    private void recoverProcessing() {
        try {
            Set<String> members = redisTemplate.opsForSet()
                    .members(RedisConstants.HOT_REPLY_PROCESSING_KEY);
            if (members != null && !members.isEmpty()) {
                redisTemplate.opsForSet().add(
                        RedisConstants.HOT_REPLY_DIRTY_KEY, members.toArray(new String[0]));
                redisTemplate.delete(RedisConstants.HOT_REPLY_PROCESSING_KEY);
            }
        } catch (Exception e) {
            log.warn("恢复热度任务 processing 集合失败", e);
        }
    }

    private Set<String> claimDirty() {
        try {
            List<String> result = redisTemplate.execute(
                    CLAIM_SCRIPT,
                    Arrays.asList(RedisConstants.HOT_REPLY_DIRTY_KEY,
                            RedisConstants.HOT_REPLY_PROCESSING_KEY),
                    String.valueOf(HotReplyConstants.DIRTY_BATCH_SIZE));
            return result == null ? Collections.emptySet() : new LinkedHashSet<>(result);
        } catch (Exception e) {
            log.warn("领取热度待重算问题失败", e);
            return Collections.emptySet();
        }
    }

    private void removeProcessing(Collection<?> members) {
        if (members == null || members.isEmpty()) {
            return;
        }
        try {
            redisTemplate.opsForSet().remove(
                    RedisConstants.HOT_REPLY_PROCESSING_KEY,
                    members.stream().map(String::valueOf).toArray(String[]::new));
        } catch (Exception e) {
            log.warn("移除 processing 标记失败：{}", members, e);
        }
    }

    private Long parseLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static List<List<Long>> partition(List<Long> list, int size) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<Long>> result = new ArrayList<>((list.size() + size - 1) / size);
        for (int i = 0; i < list.size(); i += size) {
            result.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return result;
    }
}

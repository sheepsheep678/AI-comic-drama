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
import com.aicomic.watch.utils.TopReplyIdsCodec;
import com.aicomic.watch.utils.TopReplySelector;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

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
import java.util.stream.Collectors;

/**
 * 回答点赞 Top4 与脏集合任务实现
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

        // 1.解析 DB 冗余的 Top4 回答 id；NULL 表示未计算，空串表示已计算但无可见回答
        for (InteractionQuestion question : questions) {
            Long questionId = question.getId();
            if (questionId == null) {
                continue;
            }
            List<Long> storedIds = TopReplyIdsCodec.parse(question.getTopReplyIds());
            if (storedIds == null) {
                fallbackQuestionIds.add(questionId);
                continue;
            }
            if (storedIds.isEmpty()) {
                result.put(questionId, Collections.emptyList());
                continue;
            }
            idsByQuestion.put(questionId, storedIds);
            allReplyIds.addAll(storedIds);
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
                // 字段中的回答已被删除或隐藏，实时查库重建
                fallbackQuestionIds.add(entry.getKey());
            } else {
                result.put(entry.getKey(),
                        TopReplySelector.topNByLikes(candidates, limit));
            }
        }

        // 3.未计算或字段失效的问题实时查库兜底，并打脏标记等待任务落库
        if (!fallbackQuestionIds.isEmpty()) {
            result.putAll(loadFromDatabase(fallbackQuestionIds));
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
            log.warn("标记点赞回答待重算失败：{}", questionIds, e);
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
            log.warn("将问题 Top4 回答字段置空失败：{}", ids, e);
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
        // 3.分批查询每个问题的点赞 Top4，回写 DB 字段
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
        } catch (Exception e) {
            log.warn("清理问题{}的点赞回答脏标记失败", questionId, e);
        }
    }

    /**
     * 数据库查询每个问题的点赞 Top4
     */
    private Map<Long, List<HotReplyCandidate>> loadFromDatabase(Collection<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<HotReplyCandidate> candidates;
        try {
            candidates = replyMapper.selectHotCandidates(
                    questionIds, HotReplyConstants.TOP_REPLY_SIZE);
        } catch (Exception e) {
            log.warn("查询问题点赞回答候选失败：{}", questionIds, e);
            return Collections.emptyMap();
        }
        Map<Long, List<HotReplyCandidate>> grouped = groupByQuestion(candidates);
        Map<Long, List<HotReplyCandidate>> result = new HashMap<>(questionIds.size());
        for (Long questionId : questionIds) {
            result.put(questionId, TopReplySelector.topNByLikes(
                    grouped.getOrDefault(questionId, Collections.emptyList()),
                    HotReplyConstants.TOP_REPLY_SIZE));
        }
        return result;
    }

    /**
     * 刷新一批问题：查询点赞 Top4 并回写 top_reply_ids
     */
    private void refreshQuestions(Collection<Long> questionIds) {
        List<HotReplyCandidate> candidates = replyMapper.selectHotCandidates(
                questionIds, HotReplyConstants.TOP_REPLY_SIZE);
        Map<Long, List<HotReplyCandidate>> grouped = groupByQuestion(candidates);
        for (Long questionId : questionIds) {
            List<HotReplyCandidate> top = TopReplySelector.topNByLikes(
                    grouped.getOrDefault(questionId, Collections.emptyList()),
                    HotReplyConstants.TOP_REPLY_SIZE);
            String topReplyIds = TopReplyIdsCodec.format(top.stream()
                    .map(HotReplyCandidate::getReplyId)
                    .collect(Collectors.toList()));
            InteractionQuestion update = new InteractionQuestion();
            update.setId(questionId);
            update.setTopReplyIds(topReplyIds);
            questionMapper.updateById(update);
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
            log.warn("恢复点赞回答任务 processing 集合失败", e);
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
            log.warn("领取点赞待重算问题失败", e);
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

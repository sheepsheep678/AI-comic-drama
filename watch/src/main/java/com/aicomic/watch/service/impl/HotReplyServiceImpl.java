package com.aicomic.watch.service.impl;

import com.aicomic.watch.constants.HotReplyConstants;
import com.aicomic.watch.constants.RedisConstants;
import com.aicomic.watch.domain.dto.HotReplyCandidate;
import com.aicomic.watch.domain.po.Comment;
import com.aicomic.watch.domain.po.CommentReply;
import com.aicomic.watch.mapper.CommentMapper;
import com.aicomic.watch.mapper.CommentReplyMapper;
import com.aicomic.watch.service.IHotReplyService;
import com.aicomic.watch.utils.TopReplyIdsCodec;
import com.aicomic.watch.utils.TopReplySelector;
import com.aicomic.common.autoconfigure.redisson.annotations.Lock;
import com.aicomic.common.autoconfigure.redisson.enums.LockStrategy;
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
 * 二级评论点赞 Top4 与脏集合任务实现
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
    private final CommentReplyMapper replyMapper;
    private final CommentMapper commentMapper;

    @Override
    public Map<Long, List<HotReplyCandidate>> loadHotReplies(List<Comment> comments, int limit) {
        if (comments == null || comments.isEmpty() || limit <= 0) {
            return Collections.emptyMap();
        }
        Map<Long, List<HotReplyCandidate>> result = new HashMap<>(comments.size());
        Map<Long, List<Long>> idsByComment = new HashMap<>(comments.size());
        Set<Long> allReplyIds = new HashSet<>();
        Set<Long> fallbackCommentIds = new HashSet<>();

        // 1.解析 DB 冗余的 Top4 二级评论 id；NULL 表示未计算，空串表示已计算但无可见评论
        for (Comment comment : comments) {
            Long commentId = comment.getId();
            if (commentId == null) {
                continue;
            }
            List<Long> storedIds = TopReplyIdsCodec.parse(comment.getTopReplyIds());
            if (storedIds == null) {
                fallbackCommentIds.add(commentId);
                continue;
            }
            if (storedIds.isEmpty()) {
                result.put(commentId, Collections.emptyList());
                continue;
            }
            idsByComment.put(commentId, storedIds);
            allReplyIds.addAll(storedIds);
        }

        // 2.批量加载二级评论详情，过滤缺失/隐藏
        Map<Long, CommentReply> replyMap = loadReplies(allReplyIds);
        for (Map.Entry<Long, List<Long>> entry : idsByComment.entrySet()) {
            List<HotReplyCandidate> candidates = new ArrayList<>(entry.getValue().size());
            boolean stale = false;
            for (Long replyId : entry.getValue()) {
                CommentReply reply = replyMap.get(replyId);
                if (reply == null || Boolean.TRUE.equals(reply.getHidden())) {
                    stale = true;
                    continue;
                }
                candidates.add(HotReplyCandidate.from(reply));
            }
            if (stale) {
                // 字段中的评论已被删除或隐藏，实时查库重建
                fallbackCommentIds.add(entry.getKey());
            } else {
                result.put(entry.getKey(), TopReplySelector.topNByLikes(candidates, limit));
            }
        }

        // 3.未计算或字段失效的一级评论实时查库兜底，并打脏标记等待任务落库
        if (!fallbackCommentIds.isEmpty()) {
            result.putAll(loadFromDatabase(fallbackCommentIds, limit));
            markDirty(fallbackCommentIds);
        }

        for (Comment comment : comments) {
            result.putIfAbsent(comment.getId(), Collections.emptyList());
        }
        return result;
    }

    @Override
    public boolean markDirty(Collection<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return true;
        }
        String[] members = commentIds.stream()
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
            log.warn("标记热门二级评论待重算失败：{}", commentIds, e);
            return false;
        }
    }

    @Override
    public void invalidate(Collection<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }
        List<Long> ids = commentIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        try {
            commentMapper.invalidateHotReplyIds(ids);
        } catch (Exception e) {
            log.warn("将一级评论 Top4 二级评论字段置空失败：{}", ids, e);
        }
    }

    @Override
    @Lock(name = HotReplyConstants.REFRESH_LOCK, lockStrategy = LockStrategy.SKIP_FAST)
    public void refreshDirty() {
        // 1.恢复上次异常退出时停留在 processing 的一级评论
        recoverProcessing();
        // 2.原子领取一批待重算评论
        Set<String> claimed = claimDirty();
        if (claimed == null || claimed.isEmpty()) {
            return;
        }
        List<Long> commentIds = claimed.stream()
                .map(this::parseLong)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (commentIds.isEmpty()) {
            removeProcessing(claimed);
            return;
        }
        // 3.分批查询每个一级评论的点赞 Top4，回写 DB 字段
        for (List<Long> chunk : partition(commentIds, HotReplyConstants.DIRTY_BATCH_SIZE)) {
            refreshComments(chunk);
            removeProcessing(chunk);
        }
    }

    @Override
    @Lock(name = HotReplyConstants.BACKFILL_LOCK, lockStrategy = LockStrategy.SKIP_FAST)
    public Long backfill(Long startId, int pageSize) {
        long cursor = startId == null || startId < 0 ? 0L : startId;
        int size = pageSize <= 0 ? 200 : Math.min(pageSize, 1000);
        while (true) {
            List<Comment> page = commentMapper.selectList(
                    new LambdaQueryWrapper<Comment>()
                            .gt(Comment::getId, cursor)
                            .orderByAsc(Comment::getId)
                            .last("LIMIT " + size));
            if (page == null || page.isEmpty()) {
                break;
            }
            List<Long> ids = page.stream()
                    .map(Comment::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                refreshComments(ids);
                cursor = ids.get(ids.size() - 1);
            }
            if (page.size() < size) {
                break;
            }
        }
        return cursor;
    }

    @Override
    public void cleanup(Long commentId) {
        if (commentId == null) {
            return;
        }
        try {
            redisTemplate.opsForSet().remove(RedisConstants.HOT_REPLY_DIRTY_KEY, String.valueOf(commentId));
            redisTemplate.opsForSet().remove(RedisConstants.HOT_REPLY_PROCESSING_KEY, String.valueOf(commentId));
        } catch (Exception e) {
            log.warn("清理一级评论{}的热门二级评论脏标记失败", commentId, e);
        }
    }

    /**
     * 数据库查询每个一级评论的点赞 Top4
     */
    private Map<Long, List<HotReplyCandidate>> loadFromDatabase(Collection<Long> commentIds, int limit) {
        if (commentIds == null || commentIds.isEmpty() || limit <= 0) {
            return Collections.emptyMap();
        }
        List<HotReplyCandidate> candidates;
        try {
            candidates = replyMapper.selectHotCandidates(commentIds, limit);
        } catch (Exception e) {
            log.warn("查询一级评论点赞二级评论候选失败：{}", commentIds, e);
            return Collections.emptyMap();
        }
        Map<Long, List<HotReplyCandidate>> grouped = groupByComment(candidates);
        Map<Long, List<HotReplyCandidate>> result = new HashMap<>(commentIds.size());
        for (Long commentId : commentIds) {
            result.put(commentId, TopReplySelector.topNByLikes(
                    grouped.getOrDefault(commentId, Collections.emptyList()), limit));
        }
        return result;
    }

    /**
     * 刷新一批一级评论：查询点赞 Top4 并回写 top_reply_ids
     */
    private void refreshComments(Collection<Long> commentIds) {
        List<HotReplyCandidate> candidates = replyMapper.selectHotCandidates(
                commentIds, HotReplyConstants.TOP_REPLY_SIZE);
        Map<Long, List<HotReplyCandidate>> grouped = groupByComment(candidates);
        for (Long commentId : commentIds) {
            List<HotReplyCandidate> top = TopReplySelector.topNByLikes(
                    grouped.getOrDefault(commentId, Collections.emptyList()),
                    HotReplyConstants.TOP_REPLY_SIZE);
            String topReplyIds = TopReplyIdsCodec.format(top.stream()
                    .map(HotReplyCandidate::getReplyId)
                    .collect(Collectors.toList()));
            Comment update = new Comment();
            update.setId(commentId);
            update.setTopReplyIds(topReplyIds);
            commentMapper.updateById(update);
        }
    }

    private Map<Long, CommentReply> loadReplies(Collection<Long> replyIds) {
        if (replyIds == null || replyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CommentReply> replies = replyMapper.selectBatchIds(replyIds);
        if (replies == null || replies.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, CommentReply> result = new HashMap<>(replies.size());
        for (CommentReply reply : replies) {
            if (reply != null && reply.getId() != null) {
                result.put(reply.getId(), reply);
            }
        }
        return result;
    }

    private Map<Long, List<HotReplyCandidate>> groupByComment(List<HotReplyCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<HotReplyCandidate>> grouped = new HashMap<>();
        for (HotReplyCandidate candidate : candidates) {
            if (candidate == null || candidate.getCommentId() == null) {
                continue;
            }
            grouped.computeIfAbsent(candidate.getCommentId(), key -> new ArrayList<>())
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
            log.warn("恢复热门二级评论任务 processing 集合失败", e);
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
            log.warn("领取热门二级评论待重算评论失败", e);
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

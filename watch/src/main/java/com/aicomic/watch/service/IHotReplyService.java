package com.aicomic.watch.service;

import com.aicomic.watch.domain.dto.HotReplyCandidate;
import com.aicomic.watch.domain.po.Comment;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 热门二级评论（点赞 Top4）维护
 */
public interface IHotReplyService {

    /**
     * 批量加载一级评论下的热门二级评论；优先 DB 冗余字段，失效时实时查库兜底
     */
    Map<Long, List<HotReplyCandidate>> loadHotReplies(List<Comment> comments, int limit);

    /**
     * 标记一级评论待重算
     *
     * @return Redis 是否写入成功
     */
    boolean markDirty(Collection<Long> commentIds);

    /**
     * 将一级评论的热门二级评论字段置为 NULL，强制读路径兜底
     */
    void invalidate(Collection<Long> commentIds);

    /**
     * 消费 Redis 脏集合，刷新 DB 中的热门二级评论字段
     */
    void refreshDirty();

    /**
     * 全量回填
     *
     * @return 最后处理的一级评论 id
     */
    Long backfill(Long startId, int pageSize);

    /**
     * 清理一级评论的 Redis 热度缓存
     */
    void cleanup(Long commentId);
}

package com.aicomic.watch.service;

import com.aicomic.watch.domain.dto.HotReplyCandidate;
import com.aicomic.watch.domain.po.InteractionQuestion;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 回答热度 Top1 与 Redis 排行榜
 */
public interface IHotReplyService {

    /**
     * 批量加载问题下的热度回答；优先 Redis 排行榜，其次 DB Top1，最后实时查库
     */
    Map<Long, List<HotReplyCandidate>> loadHotReplies(List<InteractionQuestion> questions, int limit);

    /**
     * 标记问题待重算
     *
     * @return Redis 是否写入成功
     */
    boolean markDirty(Collection<Long> questionIds);

    /**
     * 将问题的热度回答字段置为 NULL，强制读路径兜底
     */
    void invalidate(Collection<Long> questionIds);

    /**
     * 消费 Redis 脏集合，刷新 DB Top1 与 Redis 排行榜
     */
    void refreshDirty();

    /**
     * 全量回填
     *
     * @return 最后处理的问题 id
     */
    Long backfill(Long startId, int pageSize);

    /**
     * 清理问题的 Redis 热度缓存
     */
    void cleanup(Long questionId);
}

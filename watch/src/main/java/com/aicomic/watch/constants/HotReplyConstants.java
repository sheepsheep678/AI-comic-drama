package com.aicomic.watch.constants;

/**
 * 热门二级评论排行相关常量
 */
public interface HotReplyConstants {

    /**
     * 每个一级评论保存/返回的点赞最高二级评论数
     */
    int TOP_REPLY_SIZE = 4;
    /**
     * 单次任务处理的一级评论数
     */
    int DIRTY_BATCH_SIZE = 200;

    /**
     * 刷新任务的分布式锁
     */
    String REFRESH_LOCK = "lock:comment:reply:hot:refresh";
    /**
     * 回填任务的分布式锁
     */
    String BACKFILL_LOCK = "lock:comment:reply:hot:backfill";
}

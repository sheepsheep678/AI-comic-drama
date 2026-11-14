package com.aicomic.watch.constants;

/**
 * 回答点赞排行相关常量
 */
public interface HotReplyConstants {

    /**
     * 每个问题保存/返回的点赞最高回答数
     */
    int TOP_REPLY_SIZE = 4;
    /**
     * 单次任务处理的问题数
     */
    int DIRTY_BATCH_SIZE = 200;

    /**
     * 刷新任务的分布式锁
     */
    String REFRESH_LOCK = "lock:watch:reply:hot:refresh";
    /**
     * 回填任务的分布式锁
     */
    String BACKFILL_LOCK = "lock:watch:reply:hot:backfill";
}

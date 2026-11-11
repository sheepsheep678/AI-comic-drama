package com.aicomic.watch.constants;

/**
 * 回答热度排行榜相关常量
 */
public interface HotReplyConstants {

    /**
     * 点赞权重
     */
    int LIKED_WEIGHT = 10;
    /**
     * 回复数权重
     */
    int REPLY_WEIGHT = 2;
    /**
     * 时间衰减最大加成
     */
    double MAX_DECAY_BONUS = 20.0;
    /**
     * 时间衰减半衰期（小时）
     */
    long HALF_LIFE_HOURS = 72;
    /**
     * 时间衰减半衰期（秒）
     */
    long HALF_LIFE_SECONDS = HALF_LIFE_HOURS * 3600;

    /**
     * 接口返回的热度回答条数
     */
    int API_TOP_N = 3;
    /**
     * Redis 排行榜缓存的条数
     */
    int LEADERBOARD_SIZE = 20;
    /**
     * 每个问题最多查询的候选回答数（按点赞量倒序）
     */
    int CANDIDATE_LIMIT_PER_QUESTION = 5;
    /**
     * 单次任务处理的问题数
     */
    int DIRTY_BATCH_SIZE = 200;
    /**
     * 排行榜缓存有效期（天）
     */
    long RANK_TTL_DAYS = 7;

    /**
     * 刷新任务的分布式锁
     */
    String REFRESH_LOCK = "lock:watch:reply:hot:refresh";
    /**
     * 回填任务的分布式锁
     */
    String BACKFILL_LOCK = "lock:watch:reply:hot:backfill";
}

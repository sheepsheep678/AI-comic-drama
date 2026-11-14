package com.aicomic.watch.constants;

public interface RedisConstants {
    /**
     * 签到记录的Key的前缀：sign:uid:110:202301
     */
    String SIGN_RECORD_KEY_PREFIX = "sign:uid:";
    /**
     * 积分排行榜的Key的前缀：boards:202301
     */
    String POINTS_BOARD_KEY_PREFIX = "boards:";
    /**
     * 热度回答待重算的问题id集合
     */
    String HOT_REPLY_DIRTY_KEY = "watch:reply:hot:dirty";
    /**
     * 热度回答重算中的问题id集合
     */
    String HOT_REPLY_PROCESSING_KEY = "watch:reply:hot:processing";
}

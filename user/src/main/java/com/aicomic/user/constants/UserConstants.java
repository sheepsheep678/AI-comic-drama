package com.aicomic.user.constants;

import java.time.Duration;

public interface UserConstants {
    String DEFAULT_PASSWORD = "123456";

    Long VIEWER_ROLE_ID = 2L;
    String VIEWER_ROLE_NAME = "观众";

    Long CREATOR_ROLE_ID = 3L;
    String CREATOR_ROLE_NAME = "创作者";

    // 验证码的Redis key前缀
    String USER_VERIFY_CODE_KEY = "sms:user:code:phone:";
    // 验证码有效期，5分钟
    Duration USER_VERIFY_CODE_TTL = Duration.ofMinutes(5);
}

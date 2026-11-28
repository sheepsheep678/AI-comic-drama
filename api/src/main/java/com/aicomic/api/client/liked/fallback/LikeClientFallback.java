package com.aicomic.api.client.liked.fallback;

import com.aicomic.api.client.liked.LikeClient;
import com.aicomic.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Set;

@Slf4j
public class LikeClientFallback implements FallbackFactory<LikeClient> {

    @Override
    public LikeClient create(Throwable cause) {
        log.error("查询like-service服务异常", cause);
        return new LikeClient() {

            @Override
            public Set<Long> isBizLiked(Iterable<Long> bizIds) {
                return CollUtils.emptySet();
            }
        };
    }
}

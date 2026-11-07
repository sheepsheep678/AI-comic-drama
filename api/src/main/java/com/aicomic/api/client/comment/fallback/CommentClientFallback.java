package com.aicomic.api.client.comment.fallback;

import com.aicomic.api.client.comment.CommentClient;
import com.aicomic.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Set;

@Slf4j
public class CommentClientFallback implements FallbackFactory<CommentClient> {

    @Override
    public CommentClient create(Throwable cause) {
        log.error("查询comment-service服务异常", cause);
        return new CommentClient() {

            @Override
            public Set<Long> isBizLiked(Iterable<Long> bizIds) {
                return CollUtils.emptySet();
            }
        };
    }
}

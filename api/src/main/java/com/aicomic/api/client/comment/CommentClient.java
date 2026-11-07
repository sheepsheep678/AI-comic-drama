package com.aicomic.api.client.comment;

import com.aicomic.api.client.comment.fallback.CommentClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@FeignClient(value = "comment-service", fallbackFactory = CommentClientFallback.class)
public interface CommentClient {
    @GetMapping("/likes/list")
    Set<Long> isBizLiked(@RequestParam("bizIds") Iterable<Long> bizIds);
}

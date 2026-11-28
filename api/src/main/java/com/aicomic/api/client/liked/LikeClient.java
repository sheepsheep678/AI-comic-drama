package com.aicomic.api.client.liked;

import com.aicomic.api.client.liked.fallback.LikeClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@FeignClient(value = "like-service", fallbackFactory = LikeClientFallback.class)
public interface LikeClient {
    @GetMapping("/likes/list")
    Set<Long> isBizLiked(@RequestParam("bizIds") Iterable<Long> bizIds);
}

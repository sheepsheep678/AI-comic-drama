package com.aicomic.api.config;

import com.aicomic.api.client.watch.fallback.WatchClientFallback;
import com.aicomic.api.client.unlock.fallback.UnlockClientFallback;
import com.aicomic.api.client.liked.fallback.LikeClientFallback;
import com.aicomic.api.client.order.fallback.OrderClientFallback;
import com.aicomic.api.client.user.fallback.UserClientFallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FallbackConfig {
    @Bean
    public WatchClientFallback watchClientFallback(){
        return new WatchClientFallback();
    }

    @Bean
    public OrderClientFallback orderClientFallback(){
        return new OrderClientFallback();
    }

    @Bean
    public UserClientFallback userClientFallback(){
        return new UserClientFallback();
    }

    @Bean
    public LikeClientFallback likeClientFallback(){
        return new LikeClientFallback();
    }

    @Bean
    public UnlockClientFallback unlockClientFallback(){
        return new UnlockClientFallback();
    }
}

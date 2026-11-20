package com.aicomic.common.autoconfigure.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ 自动配置
 */
@Configuration
@ConditionalOnClass(RocketMQTemplate.class)
@AutoConfigureAfter(name = "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
public class RocketMqConfig {

    /**
     * RocketMQ 发送工具
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RocketMQTemplate.class)
    public RocketMqHelper rocketMqHelper(RocketMQTemplate rocketMQTemplate, ObjectMapper objectMapper) {
        return new RocketMqHelper(rocketMQTemplate, objectMapper);
    }
}

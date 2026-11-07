package com.aicomic.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ac.order")
public class OrderProperties {
    /**
     * 单次允许购买的最大漫剧数量
     */
    private int maxCourseAmount = 10;
    /**
     * 订单支付的最大等待时间，单位分钟
     */
    private int payOrderTTLMinutes = 30;
}

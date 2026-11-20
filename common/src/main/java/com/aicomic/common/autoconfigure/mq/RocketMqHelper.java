package com.aicomic.common.autoconfigure.mq;

import cn.hutool.core.lang.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.aicomic.common.constants.Constant.REQUEST_ID_HEADER;

/**
 * RocketMQ 发送工具
 * <p>
 * 方法参数中的 topic 对应原来的交换机，tag 对应原来的路由键。
 */
@Slf4j
public class RocketMqHelper {

    /**
     * RocketMQ 默认延迟级别：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * <p>
     * 4.x 只支持固定延迟级别，这里按“不早于期望延迟时间”的规则向上取整。
     * 如需精确延迟，可在 RocketMQ 5.x 中将 sendMessage 改为 deliveryTimestamp 方式。
     */
    private static final long[] DELAY_LEVEL_MILLIS = {
            1000L, 5000L, 10000L, 30000L, 60000L, 120000L,
            180000L, 240000L, 300000L, 360000L, 420000L, 480000L,
            540000L, 600000L, 1200000L, 1800000L, 3600000L, 7200000L
    };

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;
    private final ThreadPoolTaskExecutor executor;

    public RocketMqHelper(RocketMQTemplate rocketMQTemplate, ObjectMapper objectMapper) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.objectMapper = objectMapper;
        executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(10);
        //配置最大线程数
        executor.setMaxPoolSize(15);
        //配置队列大小
        executor.setQueueCapacity(99999);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("mq-async-send-handler");

        // 设置拒绝策略：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //执行初始化
        executor.initialize();
    }

    /**
     * 根据 topic 和 tag 发送消息
     */
    public <T> void send(String topic, String tag, T t) {
        log.debug("准备发送消息，topic：{}， tag：{}， message：{}", topic, tag, t);
        doSend(topic, tag, t, 0);
    }

    /**
     * 根据 topic 和 tag 发送消息，并且可以设置延迟时间
     */
    public <T> void sendDelayMessage(String topic, String tag, T t, Duration delay) {
        int delayLevel = toDelayLevel(delay);
        log.debug("准备发送延迟消息，topic：{}， tag：{}， delay：{}， delayLevel：{}", topic, tag, delay, delayLevel);
        doSend(topic, tag, t, delayLevel);
    }

    /**
     * 根据 topic 和 tag 异步发送消息，并指定一个延迟发送时间（本地延迟）
     *
     * @param topic Topic
     * @param tag   Tag
     * @param t     数据
     * @param time  本地延迟发送的毫秒数
     * @param <T>   数据类型
     */
    public <T> void sendAsyn(String topic, String tag, T t, Long time) {
        String requestId = MDC.get(REQUEST_ID_HEADER);
        CompletableFuture.runAsync(() -> {
            try {
                MDC.put(REQUEST_ID_HEADER, requestId);
                if (time != null && time > 0) {
                    Thread.sleep(time);
                }
                send(topic, tag, t);
            } catch (Exception e) {
                log.error("推送消息异常，t:{},", t, e);
            }
        }, executor);
    }

    /**
     * 根据 topic 和 tag 异步发送消息
     *
     * @param topic Topic
     * @param tag   Tag
     * @param t     数据
     * @param <T>   数据类型
     */
    public <T> void sendAsyn(String topic, String tag, T t) {
        sendAsyn(topic, tag, t, null);
    }

    private <T> void doSend(String topic, String tag, T t, int delayLevel) {
        // 1.设置消息标识，用于消息追踪
        String id = UUID.randomUUID().toString(true);
        // 2.传递 requestId，便于消费者日志串联
        String requestId = MDC.get(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString(true);
        }
        // 3.使用统一的 ObjectMapper 序列化，保证生产端和消费端格式一致
        String payload;
        try {
            payload = objectMapper.writeValueAsString(t);
        } catch (Exception e) {
            throw new IllegalStateException("RocketMQ消息序列化失败，topic：" + topic + "，tag：" + tag, e);
        }
        Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader(RocketMQHeaders.KEYS, id)
                .setHeader(REQUEST_ID_HEADER, requestId)
                .build();
        // 4.发送消息，destination 格式为 topic:tag
        String destination = topic + ":" + tag;
        if (delayLevel > 0) {
            rocketMQTemplate.syncSend(destination, message, 3000, delayLevel);
        } else {
            rocketMQTemplate.syncSend(destination, message);
        }
    }

    /**
     * 将延迟时间转换为 RocketMQ 延迟级别（向上取整，保证不会提前消费）
     */
    private int toDelayLevel(Duration delay) {
        long millis = Math.max(delay.toMillis(), DELAY_LEVEL_MILLIS[0]);
        for (int i = 0; i < DELAY_LEVEL_MILLIS.length; i++) {
            if (millis <= DELAY_LEVEL_MILLIS[i]) {
                return i + 1;
            }
        }
        return DELAY_LEVEL_MILLIS.length;
    }
}

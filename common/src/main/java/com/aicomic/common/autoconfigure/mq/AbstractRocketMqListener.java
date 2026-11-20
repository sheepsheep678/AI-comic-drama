package com.aicomic.common.autoconfigure.mq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import static com.aicomic.common.constants.Constant.REQUEST_ID_HEADER;

/**
 * RocketMQ 消费者基类
 * <p>
 * 统一处理 requestId 日志透传和消息反序列化。
 * 消费失败时直接抛出异常，由 RocketMQ 自带的重试机制与死信队列处理。
 */
public abstract class AbstractRocketMqListener implements RocketMQListener<MessageExt> {

    @Autowired
    protected ObjectMapper objectMapper;

    @Override
    public void onMessage(MessageExt message) {
        // 1.还原生产端的 requestId，便于日志串联
        String requestId = message.getUserProperty(REQUEST_ID_HEADER);
        String previous = MDC.get(REQUEST_ID_HEADER);
        if (requestId != null) {
            MDC.put(REQUEST_ID_HEADER, requestId);
        }
        try {
            // 2.处理消息
            handleMessage(message);
        } finally {
            // 3.恢复 MDC，避免线程复用导致 requestId 串号
            if (previous != null) {
                MDC.put(REQUEST_ID_HEADER, previous);
            } else {
                MDC.remove(REQUEST_ID_HEADER);
            }
        }
    }

    /**
     * 处理消息
     */
    protected abstract void handleMessage(MessageExt message);

    /**
     * 将消息体反序列化为指定类型
     */
    protected <T> T readMessage(MessageExt message, Class<T> type) {
        try {
            return objectMapper.readValue(message.getBody(), type);
        } catch (Exception e) {
            throw new IllegalStateException("RocketMQ消息反序列化失败，topic：" + message.getTopic()
                    + "，tag：" + message.getTags(), e);
        }
    }

    /**
     * 将消息体反序列化为指定泛型类型
     */
    protected <T> T readMessage(MessageExt message, TypeReference<T> type) {
        try {
            return objectMapper.readValue(message.getBody(), type);
        } catch (Exception e) {
            throw new IllegalStateException("RocketMQ消息反序列化失败，topic：" + message.getTopic()
                    + "，tag：" + message.getTags(), e);
        }
    }
}

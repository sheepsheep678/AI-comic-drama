package com.aicomic.order.handler;

import com.aicomic.common.autoconfigure.mq.AbstractRocketMqListener;
import com.aicomic.order.domain.dto.OrderDelayQueryDTO;
import com.aicomic.order.service.IPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import static com.aicomic.common.constants.MqConstants.ConsumerGroup.ORDER_DELAY_QUERY_GROUP;
import static com.aicomic.common.constants.MqConstants.Tag.ORDER_DELAY_TAG;
import static com.aicomic.common.constants.MqConstants.Topic.ORDER_DELAY_TOPIC;

/**
 * 订单支付状态延迟查询
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = ORDER_DELAY_TOPIC,
        selectorExpression = ORDER_DELAY_TAG,
        consumerGroup = ORDER_DELAY_QUERY_GROUP
)
public class OrderDelayMessageHandler extends AbstractRocketMqListener {

    private final IPayService payService;

    @Override
    protected void handleMessage(MessageExt message) {
        OrderDelayQueryDTO delayQuery = readMessage(message, OrderDelayQueryDTO.class);
        log.debug("收到订单延迟查询通知：{}", delayQuery);
        payService.queryPayResult(delayQuery);
    }
}

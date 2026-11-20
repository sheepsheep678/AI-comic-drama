package com.aicomic.search.mq;

import com.aicomic.api.dto.order.OrderBasicDTO;
import com.aicomic.common.autoconfigure.mq.AbstractRocketMqListener;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.search.service.IComicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.aicomic.common.constants.MqConstants.ConsumerGroup.SEARCH_ORDER_GROUP;
import static com.aicomic.common.constants.MqConstants.Tag.ORDER_PAY_TAG;
import static com.aicomic.common.constants.MqConstants.Tag.ORDER_REFUND_TAG;
import static com.aicomic.common.constants.MqConstants.Topic.ORDER_TOPIC;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = ORDER_TOPIC,
        selectorExpression = ORDER_PAY_TAG + " || " + ORDER_REFUND_TAG,
        consumerGroup = SEARCH_ORDER_GROUP
)
public class OrderEventListener extends AbstractRocketMqListener {

    @Autowired
    private IComicService courseService;

    @Override
    protected void handleMessage(MessageExt message) {
        OrderBasicDTO order = readMessage(message, OrderBasicDTO.class);
        if (order == null || order.getUserId() == null || CollUtils.isEmpty(order.getCourseIds())) {
            log.debug("订单事件，异常消息，信息未空");
            return;
        }
        if (ORDER_PAY_TAG.equals(message.getTags())) {
            log.debug("处理订单支付消息：{}", order);
            courseService.updateCourseSold(order.getCourseIds(), 1);
        } else if (ORDER_REFUND_TAG.equals(message.getTags())) {
            log.debug("处理订单退款消息：{}", order);
            courseService.updateCourseSold(order.getCourseIds(), -1);
        }
    }
}

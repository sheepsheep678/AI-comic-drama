package com.aicomic.watch.mq;

import com.aicomic.api.dto.order.OrderBasicDTO;
import com.aicomic.common.autoconfigure.mq.AbstractRocketMqListener;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.watch.service.IWatchEpisodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import static com.aicomic.common.constants.MqConstants.ConsumerGroup.WATCH_ORDER_CHANGE_GROUP;
import static com.aicomic.common.constants.MqConstants.Tag.ORDER_PAY_TAG;
import static com.aicomic.common.constants.MqConstants.Tag.ORDER_REFUND_TAG;
import static com.aicomic.common.constants.MqConstants.Topic.ORDER_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = ORDER_TOPIC,
        selectorExpression = ORDER_PAY_TAG + " || " + ORDER_REFUND_TAG,
        consumerGroup = WATCH_ORDER_CHANGE_GROUP
)
public class EpisodeChangeListener extends AbstractRocketMqListener {

    private final IWatchEpisodeService lessonService;

    @Override
    protected void handleMessage(MessageExt message) {
        OrderBasicDTO order = readMessage(message, OrderBasicDTO.class);
        // 1.健壮性处理
        if (order == null || order.getUserId() == null || CollUtils.isEmpty(order.getCourseIds())) {
            // 数据有误，无需处理
            log.error("接收到MQ消息有误，订单数据为空");
            return;
        }
        if (ORDER_PAY_TAG.equals(message.getTags())) {
            // 2.订单支付，添加漫剧
            log.debug("监听到用户{}的订单{}，需要添加漫剧{}到追剧列表中", order.getUserId(), order.getOrderId(), order.getCourseIds());
            lessonService.addUserLessons(order.getUserId(), order.getCourseIds());
        } else if (ORDER_REFUND_TAG.equals(message.getTags())) {
            // 2.订单退款，删除漫剧
            log.debug("监听到用户{}的订单{}要退款，需要删除漫剧{}", order.getUserId(), order.getOrderId(), order.getCourseIds());
            lessonService.deleteCourseFromLesson(order.getUserId(), order.getCourseIds().get(0));
        }
    }
}

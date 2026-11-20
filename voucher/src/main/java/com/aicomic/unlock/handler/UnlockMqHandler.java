package com.aicomic.unlock.handler;

import com.aicomic.common.autoconfigure.mq.AbstractRocketMqListener;
import com.aicomic.unlock.domain.dto.UserCouponDTO;
import com.aicomic.unlock.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import static com.aicomic.common.constants.MqConstants.ConsumerGroup.COUPON_RECEIVE_GROUP;
import static com.aicomic.common.constants.MqConstants.Tag.COUPON_RECEIVE_TAG;
import static com.aicomic.common.constants.MqConstants.Topic.UNLOCK_TOPIC;

@RequiredArgsConstructor
@Component
@RocketMQMessageListener(
        topic = UNLOCK_TOPIC,
        selectorExpression = COUPON_RECEIVE_TAG,
        consumerGroup = COUPON_RECEIVE_GROUP
)
public class UnlockMqHandler extends AbstractRocketMqListener {

    private final IUserCouponService userCouponService;

    @Override
    protected void handleMessage(MessageExt message) {
        UserCouponDTO uc = readMessage(message, UserCouponDTO.class);
        userCouponService.checkAndCreateUserCoupon(uc);
    }
}

package com.aicomic.message.handler;

import com.aicomic.api.dto.sms.SmsInfoDTO;
import com.aicomic.common.autoconfigure.mq.AbstractRocketMqListener;
import com.aicomic.message.service.ISmsService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import static com.aicomic.common.constants.MqConstants.ConsumerGroup.SMS_MESSAGE_GROUP;
import static com.aicomic.common.constants.MqConstants.Tag.SMS_MESSAGE_TAG;
import static com.aicomic.common.constants.MqConstants.Topic.SMS_TOPIC;

@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = SMS_TOPIC,
        selectorExpression = SMS_MESSAGE_TAG,
        consumerGroup = SMS_MESSAGE_GROUP
)
public class SmsMessageHandler extends AbstractRocketMqListener {

    private final ISmsService smsService;

    @Override
    protected void handleMessage(MessageExt message) {
        SmsInfoDTO smsInfoDTO = readMessage(message, SmsInfoDTO.class);
        smsService.sendMessage(smsInfoDTO);
    }
}

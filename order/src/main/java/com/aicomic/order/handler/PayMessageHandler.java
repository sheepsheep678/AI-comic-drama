package com.aicomic.order.handler;

import com.aicomic.common.autoconfigure.mq.AbstractRocketMqListener;
import com.aicomic.pay.sdk.dto.PayResultDTO;
import com.aicomic.pay.sdk.dto.RefundResultDTO;
import com.aicomic.order.service.IOrderService;
import com.aicomic.order.service.IRefundApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import static com.aicomic.common.constants.MqConstants.ConsumerGroup.ORDER_PAY_RESULT_GROUP;
import static com.aicomic.common.constants.MqConstants.Tag.PAY_SUCCESS_TAG;
import static com.aicomic.common.constants.MqConstants.Tag.REFUND_CHANGE_TAG;
import static com.aicomic.common.constants.MqConstants.Topic.PAY_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = PAY_TOPIC,
        selectorExpression = PAY_SUCCESS_TAG + " || " + REFUND_CHANGE_TAG,
        consumerGroup = ORDER_PAY_RESULT_GROUP
)
public class PayMessageHandler extends AbstractRocketMqListener {

    private final IOrderService orderService;
    private final IRefundApplyService refundApplyService;

    @Override
    protected void handleMessage(MessageExt message) {
        if (PAY_SUCCESS_TAG.equals(message.getTags())) {
            PayResultDTO payResult = readMessage(message, PayResultDTO.class);
            log.debug("收到支付成功通知：{}", payResult);
            orderService.handlePaySuccess(payResult);
        } else if (REFUND_CHANGE_TAG.equals(message.getTags())) {
            RefundResultDTO refundResult = readMessage(message, RefundResultDTO.class);
            log.debug("收到退款变更成功通知：{}", refundResult);
            refundApplyService.handleRefundResult(refundResult);
        }
    }
}

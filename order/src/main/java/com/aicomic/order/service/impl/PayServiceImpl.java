package com.aicomic.order.service.impl;

import com.aicomic.common.autoconfigure.mq.RabbitMqHelper;
import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.exceptions.BizIllegalException;
import com.aicomic.common.utils.AssertUtils;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.pay.sdk.client.PayClient;
import com.aicomic.pay.sdk.constants.PayType;
import com.aicomic.pay.sdk.dto.PayApplyDTO;
import com.aicomic.pay.sdk.dto.PayChannelDTO;
import com.aicomic.pay.sdk.dto.PayResultDTO;
import com.aicomic.order.config.OrderProperties;
import com.aicomic.order.constants.OrderCancelReason;
import com.aicomic.order.constants.OrderStatus;
import com.aicomic.order.constants.OrderErrorInfo;
import com.aicomic.order.domain.dto.OrderDelayQueryDTO;
import com.aicomic.order.domain.dto.PayApplyFormDTO;
import com.aicomic.order.domain.po.Order;
import com.aicomic.order.domain.po.OrderDetail;
import com.aicomic.order.domain.vo.PayChannelVO;
import com.aicomic.order.service.IOrderDetailService;
import com.aicomic.order.service.IOrderService;
import com.aicomic.order.service.IPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.aicomic.common.constants.MqConstants.Exchange.ORDER_DELAY_EXCHANGE;
import static com.aicomic.common.constants.MqConstants.Key.ORDER_DELAY_KEY;
import static com.aicomic.order.constants.OrderErrorInfo.ORDER_NOT_EXISTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements IPayService {

    private final PayClient payClient;
    private final IOrderService orderService;
    private final IOrderDetailService detailService;
    private final OrderProperties orderProperties;
    private final RabbitMqHelper mqHelper;

    @Override
    public List<PayChannelVO> queryPayChannels() {
        List<PayChannelDTO> list = payClient.listAllPayChannels();
        if (list == null) {
            return CollUtils.emptyList();
        }
        return list.stream()
                .filter(p -> p.getStatus() == 1)
                .map(p -> BeanUtils.copyBean(p, PayChannelVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public String applyPayOrder(PayApplyFormDTO payApply) {
        Long orderId = payApply.getOrderId();
        // 1.查询订单信息
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BadRequestException(ORDER_NOT_EXISTS);
        }
        // 2.判断订单状态
        if (!OrderStatus.NO_PAY.equalsValue(order.getStatus())) {
            // 订单已经支付或关闭
            throw new BizIllegalException(OrderErrorInfo.ORDER_ALREADY_FINISH);
        }
        // 3.判断订单是否已经超时
        if (order.getCreateTime().plusMinutes(orderProperties.getPayOrderTTLMinutes()).isBefore(LocalDateTime.now())) {
            // 订单已经超时，无法支付
            throw new BizIllegalException(OrderErrorInfo.ORDER_OVER_TIME);
        }
        // 4.查询订单详情
        List<OrderDetail> details = detailService.queryByOrderId(orderId);
        AssertUtils.isNotEmpty(details, ORDER_NOT_EXISTS);

        // 5.封装下单参数
        PayApplyDTO payApplyDTO = PayApplyDTO.builder()
                .bizOrderNo(orderId)
                .amount(order.getRealAmount())
                .orderInfo(details.get(0).getName())
                .bizUserId(order.getUserId())
                .payType(PayType.NATIVE.getValue())
                .payChannelCode(payApply.getPayChannelCode())
                .build();
        String url = payClient.applyPayOrder(payApplyDTO);
        // 6.通过延迟队列，异步查询支付结果
        sendDelayQueryMessage(OrderDelayQueryDTO.init(orderId));
        return url;
    }

    private void sendDelayQueryMessage(OrderDelayQueryDTO message) {
        mqHelper.sendDelayMessage(
                ORDER_DELAY_EXCHANGE,
                ORDER_DELAY_KEY,
                message, Duration.ofMillis(message.removeFirst()));
    }

    @Override
    public void queryPayResult(OrderDelayQueryDTO message) {
        // 1.获取订单信息
        Long orderId = message.getOrderId();
        Order order = orderService.getById(orderId);
        if (order == null) {
            log.error("要查询状态的订单：{}不存在", orderId);
            return;
        }
        // 2.判断订单状态
        if (!OrderStatus.NO_PAY.equalsValue(order.getStatus())) {
            // 订单已经支付或关闭，任务结束
            return;
        }
        // 3.查询支付状态
        PayResultDTO payResult = payClient.queryPayResult(orderId);
        int status = payResult.getStatus();
        if(PayResultDTO.SUCCESS != status){
            // 3.1.支付中或支付失败，需要重试查询
            if(message.getDelayMillis().size() == 0){
                // 重试次数用尽，如果依然未支付，则取消订单
                orderService.cancelOrder(orderId, OrderCancelReason.TIME_OUT);
                return;
            }
            // 发送延迟查询消息，再次查询支付状态
            sendDelayQueryMessage(message);
        }
        // 3.2.支付成功
        orderService.handlePaySuccess(payResult);
    }
}

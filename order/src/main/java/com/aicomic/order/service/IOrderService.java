package com.aicomic.order.service;

import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.pay.sdk.dto.PayResultDTO;
import com.aicomic.order.constants.OrderCancelReason;
import com.aicomic.order.domain.dto.PlaceOrderDTO;
import com.aicomic.order.domain.po.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.order.domain.po.OrderDetail;
import com.aicomic.order.domain.query.OrderPageQuery;
import com.aicomic.order.domain.vo.OrderConfirmVO;
import com.aicomic.order.domain.vo.OrderPageVO;
import com.aicomic.order.domain.vo.OrderVO;
import com.aicomic.order.domain.vo.PlaceOrderResultVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 订单 服务类
 * </p>
 *
 * @since 2022-08-29
 */
public interface IOrderService extends IService<Order> {

    PlaceOrderResultVO placeOrder(PlaceOrderDTO placeOrderDTO);

    @Transactional
    void saveOrderAndDetails(Order order, List<OrderDetail> orderDetails);

    void cancelOrder(Long orderId, OrderCancelReason cancelReason);

    void deleteOrder(Long id);

    PageDTO<OrderPageVO> queryMyOrderPage(OrderPageQuery pageQuery);

    OrderVO queryOrderById(Long id);

    PlaceOrderResultVO queryOrderStatus(Long orderId);

    void handlePaySuccess(PayResultDTO payResult);

    PlaceOrderResultVO claimFreeComic(Long courseId);

    OrderConfirmVO prePlaceOrder(List<Long> courseIds);

}

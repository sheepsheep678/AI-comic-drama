package com.aicomic.order.controller;


import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.order.constants.OrderCancelReason;
import com.aicomic.order.domain.dto.PlaceOrderDTO;
import com.aicomic.order.domain.query.OrderPageQuery;
import com.aicomic.order.domain.vo.OrderConfirmVO;
import com.aicomic.order.domain.vo.OrderPageVO;
import com.aicomic.order.domain.vo.OrderVO;
import com.aicomic.order.domain.vo.PlaceOrderResultVO;
import com.aicomic.order.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 订单 前端控制器
 * </p>
 *
 * @since 2022-08-29
 */
@Api(tags = "订单相关接口")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @ApiOperation("分页查询我的订单")
    @GetMapping("page")
    public PageDTO<OrderPageVO> queryMyOrderPage(OrderPageQuery pageQuery){
        return orderService.queryMyOrderPage(pageQuery);
    }

    @ApiOperation("根据id查询订单详细信息")
    @GetMapping("/{id}")
    public OrderVO queryOrderById(@ApiParam ("订单id")@PathVariable("id") Long id){
        return orderService.queryOrderById(id);
    }

    @ApiOperation("查询订单支付状态")
    @GetMapping("/{id}/status")
    public PlaceOrderResultVO queryOrderStatus(@ApiParam("订单id") @PathVariable("id") Long orderId) {
        return orderService.queryOrderStatus(orderId);
    }

    @ApiOperation("预下单接口，生成订单id，确认订单可用优惠券信息")
    @GetMapping("prePlaceOrder")
    public OrderConfirmVO prePlaceOrder(@RequestParam("courseIds")List<Long> courseIds) {
        return orderService.prePlaceOrder(courseIds);
    }

    @ApiOperation("下单接口")
    @PostMapping("placeOrder")
    public PlaceOrderResultVO placeOrder(@RequestBody @Validated PlaceOrderDTO placeOrderDTO) {
        return orderService.placeOrder(placeOrderDTO);
    }

    @ApiOperation("免费漫剧立刻领取接口")
    @PostMapping("/freeComic/{courseId}")
    public PlaceOrderResultVO claimFreeComic(@ApiParam("免费漫剧id") @PathVariable("courseId") Long courseId) {
        return orderService.claimFreeComic(courseId);
    }

    @ApiOperation("取消订单接口")
    @PutMapping("/{id}/cancel")
    public void cancelOrder(@ApiParam("要取消订单的id") @PathVariable("id") Long orderId){
        orderService.cancelOrder(orderId, OrderCancelReason.USER_CANCEL);
    }

    @ApiOperation("删除订单接口")
    @DeleteMapping("/{id}")
    public void deleteOrder(@ApiParam("要删除的订单id") @PathVariable("id") Long id) {
        orderService.deleteOrder(id);
    }
}

package com.aicomic.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.client.comic.ComicClient;
import com.aicomic.api.client.unlock.UnlockClient;
import com.aicomic.api.constants.ComicStatus;
import com.aicomic.api.dto.comic.ComicSimpleInfoDTO;
import com.aicomic.api.dto.unlock.CouponDiscountDTO;
import com.aicomic.api.dto.unlock.OrderCouponDTO;
import com.aicomic.api.dto.unlock.OrderComicDTO;
import com.aicomic.api.dto.order.OrderBasicDTO;
import com.aicomic.common.autoconfigure.mq.RabbitMqHelper;
import com.aicomic.common.constants.MqConstants;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.exceptions.BizIllegalException;
import com.aicomic.common.exceptions.DbException;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.common.utils.UserContext;
import com.aicomic.pay.sdk.dto.PayResultDTO;
import com.aicomic.order.config.OrderProperties;
import com.aicomic.order.constants.OrderCancelReason;
import com.aicomic.order.constants.OrderStatus;
import com.aicomic.order.constants.RefundStatus;
import com.aicomic.order.constants.OrderErrorInfo;
import com.aicomic.order.domain.dto.PlaceOrderDTO;
import com.aicomic.order.domain.po.Order;
import com.aicomic.order.domain.po.OrderDetail;
import com.aicomic.order.domain.query.OrderPageQuery;
import com.aicomic.order.domain.vo.*;
import com.aicomic.order.mapper.OrderMapper;
import com.aicomic.order.service.ICartService;
import com.aicomic.order.service.IOrderDetailService;
import com.aicomic.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.aicomic.common.constants.ErrorInfo.Msg.OPERATE_FAILED;
import static com.aicomic.order.constants.OrderErrorInfo.ORDER_ALREADY_FINISH;
import static com.aicomic.order.constants.OrderErrorInfo.ORDER_NOT_EXISTS;

/**
 * <p>
 * 订单 服务实现类
 * </p>
 *
 * @since 2022-08-29
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final ComicClient courseClient;
    private final IOrderDetailService detailService;
    private final ICartService cartService;
    private final OrderProperties orderProperties;
    private final RabbitMqHelper rabbitMqHelper;
    private final UnlockClient unlockClient;

    @Override
    @Transactional
    public PlaceOrderResultVO placeOrder(PlaceOrderDTO placeOrderDTO) {
        Long userId = UserContext.getUser();
        // 1.查询漫剧费用信息，如果不可购买，这里直接报错
        List<ComicSimpleInfoDTO> courseInfos = getOnShelfCourse(placeOrderDTO.getCourseIds());
        // 2.封装订单信息
        Order order = new Order();
        // 2.1.计算订单金额
        Integer totalAmount = courseInfos.stream()
                .map(ComicSimpleInfoDTO::getPrice).reduce(Integer::sum).orElse(0);
        // 2.2.计算优惠金额
        order.setDiscountAmount(0);
        List<Long> couponIds = placeOrderDTO.getCouponIds();
        CouponDiscountDTO discount = null;
        if (CollUtils.isNotEmpty(couponIds)) {
            List<OrderComicDTO> orderCourses = courseInfos.stream()
                    .map(c -> new OrderComicDTO().setId(c.getId()).setCateId(c.getThirdCateId()).setPrice(c.getPrice()))
                    .collect(Collectors.toList());
            discount = unlockClient.queryDiscountDetailByOrder(new OrderCouponDTO(couponIds, orderCourses));
            if(discount != null) {
                order.setDiscountAmount(discount.getDiscountAmount());
                order.setCouponIds(discount.getIds());
            }
        }
        Integer realAmount = totalAmount - order.getDiscountAmount();
        // 2.3.封装其它信息
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setRealAmount(realAmount);
        order.setStatus(OrderStatus.NO_PAY.getValue());
        order.setMessage(OrderStatus.NO_PAY.getProgressName());
        // 2.4.订单id
        Long orderId = placeOrderDTO.getOrderId();
        order.setId(orderId);

        // 3.封装订单详情
        List<OrderDetail> orderDetails = new ArrayList<>(courseInfos.size());
        for (ComicSimpleInfoDTO courseInfo : courseInfos) {
            Integer discountValue = discount == null ?
                    0 : discount.getDiscountDetail().getOrDefault(courseInfo.getId(), 0);
            orderDetails.add(packageOrderDetail(courseInfo, order, discountValue));
        }

        // 4.写入数据库
        saveOrderAndDetails(order, orderDetails);

        // 5.删除购物车数据
        cartService.deleteCartByUserAndCourseIds(userId, placeOrderDTO.getCourseIds());

        // 6.核销优惠券
        unlockClient.writeOffCoupon(couponIds);

        // 7.构建下单结果
        return PlaceOrderResultVO.builder()
                .orderId(orderId)
                .payAmount(realAmount)
                .status(order.getStatus())
                .payOutTime(LocalDateTime.now().plusMinutes(orderProperties.getPayOrderTTLMinutes()))
                .build();
    }

    private List<ComicSimpleInfoDTO> getOnShelfCourse(List<Long> courseIds) {
        // 1.查询漫剧
        List<ComicSimpleInfoDTO> courseInfos = courseClient.getSimpleInfoList(courseIds);
        LocalDateTime now = LocalDateTime.now();
        // 2.判断状态
        for (ComicSimpleInfoDTO courseInfo : courseInfos) {
            // 2.1.检查漫剧是否上架
            if(!ComicStatus.SHELF.equalsValue(courseInfo.getStatus())){
                throw new BizIllegalException(OrderErrorInfo.COMIC_NOT_FOR_SALE);
            }
            // 2.2.检查漫剧是否过期
            if(courseInfo.getPurchaseEndTime().isBefore(now)){
                throw new BizIllegalException(OrderErrorInfo.COMIC_EXPIRED);
            }
        }
        return courseInfos;
    }


    @Override
    @Transactional
    public PlaceOrderResultVO claimFreeComic(Long courseId) {
        Long userId = UserContext.getUser();
        // 1.查询漫剧信息
        List<Long> cIds = CollUtils.singletonList(courseId);
        List<ComicSimpleInfoDTO> courseInfos = getOnShelfCourse(cIds);
        if (CollUtils.isEmpty(courseInfos)) {
            // 漫剧不存在
            throw new BizIllegalException(OrderErrorInfo.COMIC_NOT_EXISTS);
        }
        ComicSimpleInfoDTO courseInfo = courseInfos.get(0);
        if(!courseInfo.getFree()){
            // 非免费漫剧，直接报错
            throw new BizIllegalException(OrderErrorInfo.COMIC_NOT_FREE);
        }
        // 2.创建订单
        Order order = new Order();
        // 2.1.基本信息
        order.setUserId(userId);
        order.setTotalAmount(0);
        order.setDiscountAmount(0);
        order.setRealAmount(0);
        order.setStatus(OrderStatus.CLAIMED.getValue());
        order.setFinishTime(LocalDateTime.now());
        order.setMessage(OrderStatus.CLAIMED.getProgressName());
        // 2.2.订单id
        Long orderId = IdWorker.getId(order);
        order.setId(orderId);

        // 3.订单详情
        OrderDetail detail = packageOrderDetail(courseInfo, order, 0);

        // 4.写入数据库
        saveOrderAndDetails(order, CollUtils.singletonList(detail));

        // 5.发送MQ消息，通知领取成功
        rabbitMqHelper.send(
                MqConstants.Exchange.ORDER_EXCHANGE,
                MqConstants.Key.ORDER_PAY_KEY,
                OrderBasicDTO.builder()
                        .orderId(orderId)
                        .userId(userId)
                        .courseIds(cIds)
                        .finishTime(order.getFinishTime())
                        .build()
        );
        // 6.返回vo
        return PlaceOrderResultVO.builder()
                .orderId(orderId)
                .payAmount(0)
                .status(order.getStatus())
                .build();
    }

    @Override
    public OrderConfirmVO prePlaceOrder(List<Long> courseIds) {
        // 1.查询漫剧信息
        List<ComicSimpleInfoDTO> courseInfos = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(courseInfos)) {
            throw new BizIllegalException(OrderErrorInfo.COMIC_NOT_EXISTS);
        }
        List<OrderComicVO> courses = BeanUtils.copyList(courseInfos, OrderComicVO.class);
        // 2.计算总价
        int total = courseInfos.stream().mapToInt(ComicSimpleInfoDTO::getPrice).sum();
        // 3.计算折扣
        List<OrderComicDTO> orderCourses = courseInfos.stream()
                .map(ci -> new OrderComicDTO().setId(ci.getId()).setCateId(ci.getThirdCateId()).setPrice(ci.getPrice()))
                .collect(Collectors.toList());
        List<CouponDiscountDTO> discountSolution = unlockClient.findDiscountSolution(orderCourses);
        // 4.生成订单id
        long orderId = IdWorker.getId();
        // 5.组织返回
        OrderConfirmVO vo = new OrderConfirmVO();
        vo.setOrderId(orderId);
        vo.setTotalAmount(total);
        vo.setDiscounts(discountSolution);
        vo.setCourses(courses);
        return vo;
    }

    private OrderDetail packageOrderDetail(ComicSimpleInfoDTO courseInfo, Order order, Integer discountValue) {
        OrderDetail detail = new OrderDetail();
        detail.setUserId(order.getUserId());
        detail.setOrderId(order.getId());
        detail.setStatus(order.getStatus());
        detail.setCourseId(courseInfo.getId());
        detail.setPrice(courseInfo.getPrice());
        detail.setCoverUrl(courseInfo.getCoverUrl());
        detail.setName(courseInfo.getName());
        detail.setValidDuration(courseInfo.getValidDuration());
        detail.setDiscountAmount(discountValue);
        detail.setRealPayAmount(courseInfo.getPrice() - detail.getDiscountAmount());
        return detail;
    }

    @Override
    @Transactional
    public void saveOrderAndDetails(Order order, List<OrderDetail> orderDetails) {
        // 4.1.写订单
        boolean success = save(order);
        if (!success) {
            throw new DbException(OrderErrorInfo.PLACE_ORDER_FAILED);
        }
        // 4.2.写订单详情
        if(orderDetails.size() == 1){
            success = detailService.save(orderDetails.get(0));
        }else {
            success = detailService.saveBatch(orderDetails);
        }
        if (!success) {
            throw new DbException(OrderErrorInfo.PLACE_ORDER_FAILED);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, OrderCancelReason cancelReason) {
        Long userId = UserContext.getUser();
        // 1.查询订单
        Order order = getById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BadRequestException(ORDER_NOT_EXISTS);
        }
        // 2.判断订单状态是否已经取消，幂等判断
        if(OrderStatus.CLOSED.equalsValue(order.getStatus())){
           // 订单已经取消，无需重复操作
           return;
        }
        // 3.判断订单是否未支付，只有未支付订单才可以取消
        if(!OrderStatus.NO_PAY.equalsValue(order.getStatus())){
            throw new BizIllegalException(ORDER_ALREADY_FINISH);
        }
        // 4.可以更新订单状态为取消了
        boolean success = lambdaUpdate()
                .set(Order::getStatus, OrderStatus.CLOSED.getValue())
                .set(Order::getMessage, cancelReason.getMsg())
                .set(Order::getCloseTime, LocalDateTime.now())
                .eq(Order::getStatus, OrderStatus.NO_PAY.getValue())
                .eq(Order::getId, orderId)
                .update();
        if (!success) {
            return;
        }
        // 5.更新订单条目的状态
        detailService.updateStatusByOrderId(orderId, OrderStatus.CLOSED.getValue());

        // 6.退还优惠券
        unlockClient.refundCoupon(order.getCouponIds());
    }

    @Override
    public void deleteOrder(Long id) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        // 2.查询订单
        Order order = getById(id);
        if (order == null) {
            return;
        }
        // 3.判断订单所属用户与当前登录用户是否一致
        if(!order.getUserId().equals(userId)){
            // 不一致，说明不是当前用户的订单，结束
            throw new BadRequestException("不能删除他人订单");
        }
        // 4.删除订单
        boolean success = removeById(id);
        if (!success) {
            throw new DbException(OPERATE_FAILED);
        }
    }

    @Override
    public PageDTO<OrderPageVO> queryMyOrderPage(OrderPageQuery pageQuery) {
        Long userId = UserContext.getUser();
        // 1.分页排序条件
        Page<Order> p = pageQuery.toMpPageDefaultSortByCreateTimeDesc();
        // 2.分页查询订单
        Integer status = pageQuery.getStatus();
        Page<Order> page = lambdaQuery()
                .eq(status != null, Order::getStatus, status)
                .eq(Order::getUserId, userId)
                .page(p);
        // 3.数据判断
        List<Order> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(p);
        }
        // 4.查询订单明细信息
        List<Long> orderIds = records.stream().map(Order::getId).collect(Collectors.toList());
        // 4.1.根据订单id查询订单明细
        List<OrderDetail> details = detailService.queryByOrderIds(orderIds);
        // 4.2.将订单明细分组，key是订单id，值是订单下的所有detail
        Map<Long, List<OrderDetailVO>> detailMap = details.stream()
                .map(od -> BeanUtils.copyBean(od, OrderDetailVO.class))
                .collect(Collectors.groupingBy(OrderDetailVO::getOrderId));
        // 5.转换VO
        List<OrderPageVO> list = new ArrayList<>(orderIds.size());
        for (Order record : records) {
            // 5.1.转换订单
            OrderPageVO v = BeanUtils.toBean(record, OrderPageVO.class);
            list.add(v);
            // 5.2.写入vo
            v.setDetails(detailMap.get(record.getId()));
            v.setStatusDesc(OrderStatus.desc(v.getStatus()));
        }
        return PageDTO.of(page, list);
    }

    @Override
    public OrderVO queryOrderById(Long id) {
        // 1.查询订单
        Order order = getById(id);
        if (order == null) {
            throw new BadRequestException(ORDER_NOT_EXISTS);
        }
        // 2.查询订单详情
        List<OrderDetail> details = detailService.queryByOrderId(id);
        // 3.转换VO
        // 3.1.订单
        OrderVO vo = BeanUtils.toBean(order, OrderVO.class);
        // 3.2.订单详情
        List<OrderDetailVO> dvs = BeanUtils.copyList(details, OrderDetailVO.class, (d, v) -> v.setCanRefund(
                // 订单已经支付，且 退款没有在进行中，标记为可退款状态
                OrderStatus.canRefund(d.getStatus()) && !RefundStatus.inProgress(v.getRefundStatus())
        ));
        vo.setDetails(dvs);
        // 3.3.订单进度
        vo.setProgressNodes(detailService.packageProgressNodes(order, null));

        // 3.4.优惠明细
        List<String> rules = unlockClient.queryDiscountRules(order.getCouponIds());
        vo.setCouponDesc(String.join("/", rules));
        return vo;
    }

    @Override
    public PlaceOrderResultVO queryOrderStatus(Long orderId) {
        // 1.查询订单
        Order order = getById(orderId);
        if (order == null) {
            throw new BizIllegalException(ORDER_NOT_EXISTS);
        }
        // 2.计算超时时间
        LocalDateTime outTime = null;
        if(OrderStatus.NO_PAY.equalsValue(order.getStatus())){
            outTime = order.getCreateTime().plusMinutes(orderProperties.getPayOrderTTLMinutes());
        }
        // 3.封装结果
        return PlaceOrderResultVO.builder()
                .orderId(orderId)
                .payAmount(order.getRealAmount())
                .status(order.getStatus())
                .payOutTime(outTime)
                .build();
    }

    @Override
    @Transactional
    public void handlePaySuccess(PayResultDTO payResult) {
        // 1.查询订单
        Order order = getById(payResult.getBizOrderId());
        if (order == null) {
            return;
        }
        // 2.更新订单状态
        Order o = new Order();
        o.setId(order.getId());
        o.setStatus(OrderStatus.PAYED.getValue());
        o.setPayTime(payResult.getSuccessTime());
        o.setPayChannel(payResult.getPayChannel());
        o.setPayOrderNo(payResult.getPayOrderNo());
        o.setMessage("用户支付成功");
        updateById(o);
        // 3.更新订单条目
        detailService.markDetailSuccessByOrderId(o.getId(), payResult.getPayChannel(), payResult.getSuccessTime());
        // 4.查询订单包含的漫剧信息
        List<Long> cIds = detailService.queryCourseIdsByOrderId(o.getId());
        // 5.发送MQ消息，通知领取成功
        rabbitMqHelper.send(
                MqConstants.Exchange.ORDER_EXCHANGE,
                MqConstants.Key.ORDER_PAY_KEY,
                OrderBasicDTO.builder()
                        .orderId(o.getId()).userId(order.getUserId()).courseIds(cIds)
                        .finishTime(o.getPayTime())
                        .build()
        );
    }

}

package com.aicomic.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.api.dto.comic.ComicPurchaseInfoDTO;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.order.domain.po.Order;
import com.aicomic.order.domain.po.OrderDetail;
import com.aicomic.order.domain.po.RefundApply;
import com.aicomic.order.domain.query.OrderDetailPageQuery;
import com.aicomic.order.domain.vo.OrderDetailAdminVO;
import com.aicomic.order.domain.vo.OrderDetailPageVO;
import com.aicomic.order.domain.vo.OrderProgressNodeVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单明细 服务类
 * </p>
 *
 * @since 2022-08-29
 */
public interface IOrderDetailService extends IService<OrderDetail> {

    void updateStatusByOrderId(Long orderId, Integer status);

    List<OrderDetail> queryByOrderIds(List<Long> orderIds);

    List<OrderDetail> queryByOrderId(Long orderId);

    PageDTO<OrderDetailPageVO> queryDetailForPage(OrderDetailPageQuery pageQuery);

    OrderDetailAdminVO queryOrdersDetailProgress(Long id);

    List<OrderProgressNodeVO> packageProgressNodes(Order order, RefundApply refundApply);

    void markDetailSuccessByOrderId(Long id, String payChannel, LocalDateTime successTime);

    void updateRefundStatusById(Long orderDetailId, int status);

    List<Long> queryCourseIdsByOrderId(Long orderId);

    Boolean checkCourseOrderInfo(Long courseId);

    Map<Long, Integer> countPurchaseNumOfComic(List<Long> courseIdList);

    Map<Long, Integer> countPurchaseComicOfViewer(List<Long> viewerIds);

    ComicPurchaseInfoDTO getPurchaseInfoOfCourse(Long courseId);
}

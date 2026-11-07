package com.aicomic.pay.service;

import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.pay.domain.po.RefundOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.pay.sdk.dto.RefundApplyDTO;
import com.aicomic.pay.sdk.dto.RefundResultDTO;

/**
 * <p>
 * 退款订单 服务类
 * </p>
 *
 * @since 2022-08-26
 */
public interface IRefundOrderService extends IService<RefundOrder> {

    RefundResultDTO applyRefund(RefundApplyDTO refundApplyDTO);

    RefundResultDTO queryRefundResult(Long bizRefundOrderId);

    RefundOrder queryByRefundOrderNo(Long refundOrderNo);

    PageDTO<RefundOrder> queryRefundingOrderByPage(int pageNo, int size);

    void checkRefundOrder(RefundOrder refundOrder);

    RefundResultDTO queryRefundDetail(Long bizRefundOrderId);
}

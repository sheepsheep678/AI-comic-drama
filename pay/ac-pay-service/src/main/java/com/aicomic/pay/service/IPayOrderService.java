package com.aicomic.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.pay.domain.po.PayOrder;
import com.aicomic.pay.sdk.dto.PayApplyDTO;
import com.aicomic.pay.sdk.dto.PayResultDTO;

import java.time.LocalDateTime;

/**
 * <p>
 * 支付订单 服务类
 * </p>
 *
 * @since 2022-08-26
 */
public interface IPayOrderService extends IService<PayOrder> {

    String applyPayOrder(PayApplyDTO payApplyDTO);

    PayOrder queryByBizOrderNo(Long bizOrderNo);

    PayResultDTO queryPayResult(Long bizOrderId);

    PayOrder queryByPayOrderNo(Long payOrderNo);

    boolean markPayOrderSuccess(Long id, LocalDateTime successTime);

    PageDTO<PayOrder> queryPayingOrderByPage(int page, int size);

    void checkPayOrder(PayOrder payOrder);
}

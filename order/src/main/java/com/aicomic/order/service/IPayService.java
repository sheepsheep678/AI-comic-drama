package com.aicomic.order.service;

import com.aicomic.order.domain.dto.OrderDelayQueryDTO;
import com.aicomic.order.domain.dto.PayApplyFormDTO;
import com.aicomic.order.domain.vo.PayChannelVO;

import java.util.List;

public interface IPayService {
    List<PayChannelVO> queryPayChannels();

    String applyPayOrder(PayApplyFormDTO payApply);

    void queryPayResult(OrderDelayQueryDTO message);
}

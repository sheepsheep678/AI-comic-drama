package com.aicomic.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.pay.sdk.dto.RefundResultDTO;
import com.aicomic.order.domain.dto.ApproveFormDTO;
import com.aicomic.order.domain.dto.RefundCancelDTO;
import com.aicomic.order.domain.dto.RefundFormDTO;
import com.aicomic.order.domain.po.RefundApply;
import com.aicomic.order.domain.query.RefundApplyPageQuery;
import com.aicomic.order.domain.vo.RefundApplyPageVO;
import com.aicomic.order.domain.vo.RefundApplyVO;

import java.util.List;

/**
 * <p>
 * 退款申请 服务类
 * </p>
 *
 * @since 2022-08-29
 */
public interface IRefundApplyService extends IService<RefundApply> {

    List<RefundApply> queryByDetailId(Long id);

    void applyRefund(RefundFormDTO refundFormDTO);

    PageDTO<RefundApplyPageVO> queryRefundApplyByPage(RefundApplyPageQuery pageQuery);

    RefundApplyVO queryRefundDetailById(Long id);

    RefundApplyVO nextRefundApplyToApprove();

    void approveRefundApply(ApproveFormDTO approveDTO);

    void cancelRefundApply(RefundCancelDTO cancelDTO);

    RefundApplyVO queryRefundDetailByDetailId(Long id);

    void handleRefundResult(RefundResultDTO refundResult);

    List<RefundApply> queryApplyToSend(int page, int size);

    void sendRefundRequest(RefundApply refundApply);

    boolean checkRefundStatus(RefundApply refundApply);
}

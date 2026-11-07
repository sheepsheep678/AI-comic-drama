package com.aicomic.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.cache.RoleCache;
import com.aicomic.api.client.user.UserClient;
import com.aicomic.api.dto.order.OrderBasicDTO;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.common.autoconfigure.mq.RabbitMqHelper;
import com.aicomic.common.constants.Constant;
import com.aicomic.common.constants.ErrorInfo;
import com.aicomic.common.constants.MqConstants;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.enums.UserType;
import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.exceptions.BizIllegalException;
import com.aicomic.common.exceptions.DbException;
import com.aicomic.common.utils.*;
import com.aicomic.pay.sdk.client.PayClient;
import com.aicomic.pay.sdk.constants.PayChannel;
import com.aicomic.pay.sdk.constants.RefundChannelEnum;
import com.aicomic.pay.sdk.dto.RefundApplyDTO;
import com.aicomic.pay.sdk.dto.RefundResultDTO;
import com.aicomic.order.constants.OrderStatus;
import com.aicomic.order.constants.RefundStatus;
import com.aicomic.order.constants.OrderErrorInfo;
import com.aicomic.order.domain.dto.ApproveFormDTO;
import com.aicomic.order.domain.dto.RefundCancelDTO;
import com.aicomic.order.domain.dto.RefundFormDTO;
import com.aicomic.order.domain.po.Order;
import com.aicomic.order.domain.po.OrderDetail;
import com.aicomic.order.domain.po.RefundApply;
import com.aicomic.order.domain.query.RefundApplyPageQuery;
import com.aicomic.order.domain.vo.RefundApplyPageVO;
import com.aicomic.order.domain.vo.RefundApplyVO;
import com.aicomic.order.mapper.OrderMapper;
import com.aicomic.order.mapper.RefundApplyMapper;
import com.aicomic.order.service.IOrderDetailService;
import com.aicomic.order.service.IRefundApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.aicomic.order.constants.RefundStatus.AGREE;
import static com.aicomic.order.constants.RefundStatus.REJECT;

/**
 * <p>
 * 退款申请 服务实现类
 * </p>
 *
 * @since 2022-08-29
 */
@Service
@RequiredArgsConstructor
public class RefundApplyServiceImpl extends ServiceImpl<RefundApplyMapper, RefundApply> implements IRefundApplyService {

    private final OrderMapper orderMapper;
    private final IOrderDetailService detailService;
    private final UserClient userClient;
    private final PayClient payClient;
    private final RoleCache roleCache;
    private final ThreadPoolTaskExecutor sendRefundRequestExecutor;
    private final RabbitMqHelper rabbitMqHelper;

    @Override
    public List<RefundApply> queryByDetailId(Long id) {
        // 1.根据id倒序查询，最新的退款申请在最前面
        List<RefundApply> list = baseMapper.queryByDetailId(id);
        // 2.判空
        if (CollUtils.isEmpty(list)) {
            return CollUtils.emptyList();
        }
        return list;
    }

    @Override
    @Transactional
    public void applyRefund(RefundFormDTO refundFormDTO) {
        Long userId = UserContext.getUser();
        // 1.查询订单明细
        OrderDetail detail = detailService.getBaseMapper().selectById(refundFormDTO.getOrderDetailId());
        if (detail == null) {
            throw new BadRequestException(OrderErrorInfo.ORDER_NOT_EXISTS);
        }
        if (detail.getRealPayAmount() == 0) {
            // 免费漫剧不能退款
            throw new BadRequestException(OrderErrorInfo.FREE_COMIC_CANNOT_REFUND);
        }
        // 2.查询订单
        Order order = orderMapper.getById(detail.getOrderId());
        if(order == null){
            throw new BadRequestException(OrderErrorInfo.ORDER_NOT_EXISTS);
        }
        if(!(OrderStatus.PAYED.equalsValue(order.getStatus()) || OrderStatus.REFUNDED.equalsValue(order.getStatus()))){
            // 订单状态未支付或已经完结，不能退款
            throw new BizIllegalException(OrderErrorInfo.ORDER_CANNOT_REFUND);
        }

        // 3.查询申请人信息
        UserDTO userDTO = userClient.queryUserById(userId);
        AssertUtils.isNotNull(userDTO, ErrorInfo.Msg.USER_NOT_EXISTS);
        boolean isViewer = UserType.VIEWER.equalsValue(userDTO.getType());
        if (!userId.equals(detail.getUserId()) && isViewer) {
            // 申请人不是订单中的用户，并且申请人也不是后台管理员，直接报错
            throw new BizIllegalException(OrderErrorInfo.NO_AUTH_REFUND);
        }

        // 4.查询已经申请的退款次数
        List<RefundApply> refundApplies = queryByDetailId(refundFormDTO.getOrderDetailId());
        if (isViewer && refundApplies.size() >= 2) {
            throw new BizIllegalException(OrderErrorInfo.REFUND_TOO_MANY_TIMES);
        }
        // 5.判断最近一次退款的状态，如果退款在进行中，直接返回
        if (CollUtils.isNotEmpty(refundApplies) && RefundStatus.inProgress(refundApplies.get(0).getStatus())) {
            throw new BizIllegalException(OrderErrorInfo.REFUND_IN_PROGRESS);
        }

        // 6.提交退款申请
        RefundApply refundApply = new RefundApply();
        refundApply.setOrderDetailId(detail.getId()); //订单明细id
        refundApply.setOrderId(detail.getOrderId()); //订单id
        refundApply.setUserId(detail.getUserId()); //退款订单所属人
        refundApply.setRefundAmount(detail.getRealPayAmount()); //退款金额
        refundApply.setRefundReason(refundFormDTO.getRefundReason()); //退款原因
        refundApply.setQuestionDesc(refundFormDTO.getQuestionDesc()); //退款问题说明
        refundApply.setCreater(userId); //申请id
        if (isViewer) {
            refundApply.setMessage("用户申请退款");
            refundApply.setStatus(RefundStatus.UN_APPROVE.getValue());
        } else {
            refundApply.setMessage("管理员直接退款");
            refundApply.setStatus(AGREE.getValue());
        }
        boolean success = save(refundApply);
        if (!success) {
            // 退款申请失败
            throw new DbException(ErrorInfo.Msg.DB_SAVE_EXCEPTION);
        }
        // 7.更新订单状态
        Order o = new Order();
        o.setId(refundApply.getOrderId());
        o.setStatus(OrderStatus.REFUNDED.getValue());
        o.setRefundTime(LocalDateTime.now());
        o.setMessage(isViewer ? "观众申请退款" : "管理员直接退款");
        int count = orderMapper.updateById(o);
        if (count < 1) {
            // 退款申请失败
            throw new DbException(ErrorInfo.Msg.DB_SAVE_EXCEPTION);
        }
        // 8.更新订单详情状态
        OrderDetail d = new OrderDetail();
        d.setId(detail.getId());
        d.setStatus(OrderStatus.REFUNDED.getValue());
        d.setRefundStatus(refundApply.getStatus());
        detailService.updateById(d);
        // 9.如果是管理员申请的，立刻异步发送退款请求
        if(!isViewer) {
            sendRefundRequestAsync(refundApply);
        }
    }

    @Override
    public PageDTO<RefundApplyPageVO> queryRefundApplyByPage(RefundApplyPageQuery q) {
        // 1.分页和排序条件
        Page<RefundApply> p = searchRefundApply(q);

        // 2.数据处理
        List<RefundApply> records = p.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(p);
        }
        // 3.获取用户信息
        Map<Long, UserDTO> userMap = getRefundUserInfo(records);
        // 4.vo转换
        List<RefundApplyPageVO> list = new ArrayList<>(records.size());
        for (RefundApply r : records) {
            RefundApplyPageVO v = BeanUtils.copyBean(r, RefundApplyPageVO.class);
            list.add(v);
            // 4.1.申请人
            UserDTO u = userMap.get(r.getCreater());
            v.setProposerName(roleCache.exchangeRoleName(u));
            v.setProposerMobile(u == null ? null : u.getCellPhone());
            // 4.2.审批人
            v.setApproverName(roleCache.exchangeRoleName(userMap.get(r.getApprover())));
            // 4.3.退款状态
            v.setRefundStatusDesc(RefundStatus.desc(r.getStatus()));
            if (RefundStatus.SUCCESS.equalsValue(r.getStatus())) {
                v.setRefundSuccessTime(r.getFinishTime());
            }
        }
        return PageDTO.of(p, list);
    }

    private Page<RefundApply> searchRefundApply(RefundApplyPageQuery q) {
        Integer refundStatus = q.getRefundStatus();
        String defaultSortBy = "id";
        boolean isAsc = true;
        if (refundStatus != null) {
            if (RefundStatus.UN_APPROVE.equalsValue(refundStatus)) {
                defaultSortBy = Constant.DATA_FIELD_NAME_CREATE_TIME;
            } else {
                defaultSortBy = "approve_time";
                isAsc = false;
            }
        }
        Page<RefundApply> p = q.toMpPage(defaultSortBy, isAsc);

        // 2.观众条件
        Long userId = null;
        if (StringUtils.isNotBlank(q.getMobile())) {
            userId = userClient.exchangeUserIdWithPhone(q.getMobile());
            if (userId == null) {
                // 观众不存在，则返回空数据
                return Page.of(0, 0);
            }
        }

        // 3.分页搜索
        p = lambdaQuery()
                .eq(q.getId() != null, RefundApply::getId, q.getId())
                .eq(refundStatus != null, RefundApply::getStatus, refundStatus)
                .eq(q.getOrderDetailId() != null, RefundApply::getOrderDetailId, q.getOrderDetailId())
                .eq(q.getOrderId() != null, RefundApply::getOrderId, q.getOrderId())
                .eq(userId != null, RefundApply::getUserId, userId)
                .ge(q.getApplyStartTime() != null, RefundApply::getCreateTime, q.getApplyStartTime())
                .le(q.getApplyEndTime() != null, RefundApply::getCreateTime, q.getApplyEndTime())
                .page(p);
        return p;
    }

    private Map<Long, UserDTO> getRefundUserInfo(List<RefundApply> records) {
        Set<Long> uIds = new HashSet<>();
        for (RefundApply record : records) {
            uIds.add(record.getCreater());
            uIds.add(record.getApprover());
        }
        uIds.remove(null);
        List<UserDTO> userDTOS = userClient.queryUserByIds(uIds);
        if (userDTOS.size() != uIds.size()) {
            throw new BizIllegalException("用户数据有误");
        }
        return userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
    }

    @Override
    public RefundApplyVO queryRefundDetailById(Long id) {
        // 1.查询退款数据
        RefundApply apply = getById(id);
        if (apply == null) {
            throw new BadRequestException(OrderErrorInfo.REFUND_NOT_EXISTS);
        }
        // 2.转换VO
        RefundApplyVO vo = BeanUtils.copyBean(apply, RefundApplyVO.class);

        // 3.查询订单信息及交易流水
        Order order = orderMapper.getById(apply.getOrderId());
        if (order == null) {
            throw new BadRequestException(OrderErrorInfo.ORDER_NOT_EXISTS);
        }
        vo.setPayOrderNo(order.getPayOrderNo());
        vo.setPayChannel(PayChannel.desc(order.getPayChannel()));
        vo.setRefundChannel(RefundChannelEnum.desc(apply.getRefundChannel()));
        vo.setOrderTime(order.getCreateTime());
        vo.setPaySuccessTime(order.getPayTime());

        // 4.用户信息
        Set<Long> uIds = new HashSet<>(2);
        uIds.add(apply.getUserId());
        uIds.add(apply.getCreater());
        // 4.1.远程查询
        List<UserDTO> userDTOS = userClient.queryUserByIds(uIds);
        AssertUtils.isNotEmpty(userDTOS, OrderErrorInfo.COMIC_EXPIRED);
        Map<Long, UserDTO> userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        // 4.2.观众
        UserDTO viewer = userMap.get(apply.getUserId());
        vo.setViewerName(roleCache.exchangeRoleName(viewer));
        vo.setMobile(viewer.getCellPhone());
        // 4.3.申请人
        vo.setRefundProposerName(roleCache.exchangeRoleName(userMap.get(apply.getCreater())));

        // 6.订单详情
        OrderDetail detail = detailService.getBaseMapper().selectById(apply.getOrderDetailId());
        if (detail == null) {
            throw new BadRequestException(OrderErrorInfo.ORDER_NOT_EXISTS);
        }
        vo.setName(detail.getName());
        vo.setPrice(detail.getPrice());
        vo.setRealPayAmount(detail.getRealPayAmount());
        vo.setDiscountAmount(detail.getDiscountAmount());

        return vo;
    }

    @Override
    public RefundApplyVO nextRefundApplyToApprove() {
        // 1.查询一个待处理的申请单
        Long id = baseMapper.nextRefundApplyToApprove();
        // 2.查询数据并返回
        return queryRefundDetailById(id);
    }

    @Override
    @Transactional
    public void approveRefundApply(ApproveFormDTO approveDTO) {
        // 1.查询申请
        RefundApply apply = getById(approveDTO.getId());
        if (apply == null) {
            throw new BadRequestException(OrderErrorInfo.REFUND_NOT_EXISTS);
        }
        // 2.判断状态
        if (!RefundStatus.UN_APPROVE.equalsValue(apply.getStatus())) {
            // 已审批订单，无法再次审批
            throw new BadRequestException(OrderErrorInfo.REFUND_APPROVED);
        }
        // 3.更新数据
        boolean agree = approveDTO.getApproveType() == 1;
        RefundApply r = new RefundApply();
        r.setId(apply.getId());
        r.setApprover(UserContext.getUser());
        r.setStatus(agree ? AGREE.getValue() : REJECT.getValue());
        r.setApproveTime(LocalDateTime.now());
        r.setApproveOpinion(approveDTO.getApproveOpinion());
        r.setRemark(approveDTO.getRemark());
        r.setMessage(RefundStatus.desc(r.getStatus()));
        boolean success = updateById(r);

        if (!success) {
            throw new DbException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
        }

        // 4.更新子订单状态
        detailService.updateRefundStatusById(apply.getOrderDetailId(), r.getStatus());

        // 5.异步发送退款请求
        if(agree) {
            sendRefundRequestAsync(apply);
        }
    }

    @Override
    @Transactional
    public void cancelRefundApply(RefundCancelDTO cancelDTO) {
        // 1.查询退款申请记录
        Long applyId = cancelDTO.getId();
        Long detailId = cancelDTO.getOrderDetailId();
        List<RefundApply> list = lambdaQuery()
                .eq(applyId != null, RefundApply::getId, applyId)
                .eq(detailId != null, RefundApply::getOrderDetailId, detailId)
                .list();
        // 2.判断是否为空
        if (CollUtils.isEmpty(list)) {
            return;
        }
        // 3.获取最新一次退款记录，判断状态
        RefundApply apply = list.get(0);
        if (!RefundStatus.UN_APPROVE.equalsValue(apply.getStatus())) {
            // 申请已经完成审批或退款，不能撤销
            throw new BizIllegalException(OrderErrorInfo.REFUND_APPROVED);
        }
        // 4.更新退款记录
        RefundApply r = new RefundApply();
        r.setId(applyId);
        r.setStatus(RefundStatus.CANCEL.getValue());
        r.setMessage(RefundStatus.CANCEL.getProgressName());
        boolean success = updateById(r);
        if (!success) {
            throw new DbException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
        }
        // 4.更新子订单状态
        detailService.updateRefundStatusById(r.getOrderDetailId(), r.getStatus());
    }

    @Override
    public RefundApplyVO queryRefundDetailByDetailId(Long detailId) {
        // 1.查询申请记录
        List<RefundApply> refundApplies = queryByDetailId(detailId);
        if (CollUtils.isEmpty(refundApplies)) {
            return null;
        }

        // 2.获取最近一次记录，转换vo
        RefundApply apply = refundApplies.get(0);
        RefundApplyVO vo = BeanUtils.copyBean(apply, RefundApplyVO.class);

        // 3.查询订单信息
        Order order = orderMapper.getById(apply.getOrderId());
        vo.setPayOrderNo(order.getPayOrderNo());
        vo.setOrderTime(order.getCreateTime());
        vo.setPaySuccessTime(order.getPayTime());

        vo.setPayChannel(PayChannel.desc(order.getPayChannel()));
        vo.setRefundChannel(RefundChannelEnum.desc(apply.getRefundChannel()));
        vo.setRefundOrderNo(apply.getRefundOrderNo());
        return vo;
    }

    @Override
    @Transactional
    public void handleRefundResult(RefundResultDTO result) {
        // 1.查询退款申请记录
        RefundApply refundApply = getById(result.getBizRefundOrderId());
        if (refundApply == null) {
            return;
        }
        UserContext.setUser(refundApply.getApprover());
        // 2.判断结果，支付宝支付有可能直接返回退款成功结果，微信只会返回退款中
        RefundApply r = new RefundApply();
        r.setId(refundApply.getId());
        r.setRefundChannel(result.getRefundChannel());
        r.setRefundOrderNo(result.getRefundOrderNo());
        // 2.1.判断状态是否退款中
        int status = result.getStatus();
        if(status == RefundResultDTO.RUNNING){
            // 退款中，结果未知，将其它数据写入数据库即可
            updateById(r);
            return;
        }

        // 2.2.判断退款成功还是失败
        if(status == RefundResultDTO.SUCCESS){
            // 退款成功，记录状态
            r.setStatus(RefundStatus.SUCCESS.getValue());
            r.setMessage(RefundStatus.SUCCESS.getProgressName());
        }else {
            // 2.3.退款失败，需要记录状态及退款失败原因
            r.setStatus(RefundStatus.FAILED.getValue());
            r.setMessage(RefundStatus.FAILED.getProgressName());
            r.setFailedReason(result.getMsg());
        }

        // 2.4.更新数据库
        r.setFinishTime(LocalDateTime.now());
        updateById(r);

        // 3.更新子订单状态
        detailService.updateRefundStatusById(refundApply.getOrderDetailId(), r.getStatus());

        // 4.如果是退款成功，要取消用户领取的漫剧
        if (status == RefundResultDTO.SUCCESS) {
            // 4.1.查询子订单信息
            OrderDetail detail = detailService.getById(refundApply.getOrderDetailId());
            // 4.2.发送MQ消息，通知领取成功
            rabbitMqHelper.send(
                    MqConstants.Exchange.ORDER_EXCHANGE,
                    MqConstants.Key.ORDER_REFUND_KEY,
                    OrderBasicDTO.builder()
                            .orderId(refundApply.getOrderId())
                            .userId(refundApply.getUserId())
                            .courseIds(CollUtils.singletonList(detail.getCourseId())).build());
        }
    }

    @Override
    public List<RefundApply> queryApplyToSend(int index, int size) {
        Page<RefundApply> page = lambdaQuery()
                .eq(RefundApply::getStatus, AGREE.getValue())
                .page(new Page<>(index, size));
        if (page == null || CollUtils.isEmpty(page.getRecords())) {
            return CollUtils.emptyList();
        }
        return page.getRecords();
    }

    @Override
    @Transactional
    public void sendRefundRequest(RefundApply refundApply) {
        // 1.组织请求参数
        RefundApplyDTO applyDTO = RefundApplyDTO.builder()
                .bizOrderNo(refundApply.getOrderId())
                .bizRefundOrderNo(refundApply.getId())
                .refundAmount(refundApply.getRefundAmount())
                .build();
        // 2.发送退款请求
        RefundResultDTO result = payClient.applyRefund(applyDTO);

        // 3.处理退款结果
        handleRefundResult(result);
    }

    @Override
    @Transactional
    public boolean checkRefundStatus(RefundApply refundApply) {
        // 1.先检查是否已经退款成功
        Integer status = refundApply.getStatus();
        if(!AGREE.equalsValue(status)){
            return true;
        }
        // 2.远程查询，判断是否已经退款成功
        RefundResultDTO result = payClient.queryRefundResult(refundApply.getId());
        if (result == null) {
            // 退款数据不存在，放弃处理
            return false;
        }
        // 3.处理退款结果
        handleRefundResult(result);
        return result.getStatus() != RefundResultDTO.RUNNING;
    }

    private void sendRefundRequestAsync(RefundApply refundApply) {
        sendRefundRequestExecutor.execute(() -> this.sendRefundRequest(refundApply));
    }
}

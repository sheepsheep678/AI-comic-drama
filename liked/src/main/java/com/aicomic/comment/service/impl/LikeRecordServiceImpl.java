package com.aicomic.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.dto.comment.LikeTimesDTO;
import com.aicomic.common.autoconfigure.mq.RabbitMqHelper;
import com.aicomic.common.utils.StringUtils;
import com.aicomic.common.utils.UserContext;
import com.aicomic.comment.domain.dto.LikeRecordFormDTO;
import com.aicomic.comment.domain.po.LikeRecord;
import com.aicomic.comment.mapper.LikeRecordMapper;
import com.aicomic.comment.service.ILikeRecordService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.aicomic.common.constants.MqConstants.Exchange.LIKE_RECORD_EXCHANGE;
import static com.aicomic.common.constants.MqConstants.Key.LIKE_TIMES_KEY_TEMPLATE;

/**
 * <p>
 * 点赞记录表 服务实现类
 * </p>
 *
 */
// @Service
@RequiredArgsConstructor
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements ILikeRecordService {

    private final RabbitMqHelper mqHelper;

    @Override
    public void addLikeRecord(LikeRecordFormDTO recordDTO) {
        // 1.基于前端的参数，判断是执行点赞还是取消点赞
        boolean success = recordDTO.getLiked() ? like(recordDTO) : unlike(recordDTO);
        // 2.判断是否执行成功，如果失败，则直接结束
        if (!success) {
            return;
        }
        // 3.如果执行成功，统计点赞总数
        Integer likedTimes = lambdaQuery()
                .eq(LikeRecord::getBizId, recordDTO.getBizId())
                .count();
        // 4.发送MQ通知
        mqHelper.send(
                LIKE_RECORD_EXCHANGE,
                StringUtils.format(LIKE_TIMES_KEY_TEMPLATE, recordDTO.getBizType()),
                LikeTimesDTO.of(recordDTO.getBizId(), likedTimes));
    }

    @Override
    public Set<Long> isBizLiked(List<Long> bizIds) {
        // 1.获取登录用户id
        Long userId = UserContext.getUser();
        // 2.查询点赞状态
        List<LikeRecord> list = lambdaQuery()
                .in(LikeRecord::getBizId, bizIds)
                .eq(LikeRecord::getUserId, userId)
                .list();
        // 3.返回结果
        return list.stream().map(LikeRecord::getBizId).collect(Collectors.toSet());
    }

    @Override
    public void readLikedTimesAndSendMessage(String bizType, int maxBizSize) {

    }

    private boolean unlike(LikeRecordFormDTO recordDTO) {
        return remove(new QueryWrapper<LikeRecord>().lambda()
                .eq(LikeRecord::getUserId, UserContext.getUser())
                .eq(LikeRecord::getBizId, recordDTO.getBizId()));
    }

    private boolean like(LikeRecordFormDTO recordDTO) {
        Long userId = UserContext.getUser();
        // 1.查询点赞记录
        Integer count = lambdaQuery()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getBizId, recordDTO.getBizId())
                .count();
        // 2.判断是否存在，如果已经存在，直接结束
        if (count > 0) {
            return false;
        }
        // 3.如果不存在，直接新增
        LikeRecord r = new LikeRecord();
        r.setUserId(userId);
        r.setBizId(recordDTO.getBizId());
        r.setBizType(recordDTO.getBizType());
        save(r);
        return true;
    }
}

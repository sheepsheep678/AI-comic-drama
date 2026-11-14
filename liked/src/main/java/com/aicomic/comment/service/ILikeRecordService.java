package com.aicomic.comment.service;

import com.aicomic.comment.domain.dto.LikeRecordFormDTO;
import com.aicomic.comment.domain.po.LikeRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 点赞记录表 服务类
 * </p>
 *
 */
public interface ILikeRecordService extends IService<LikeRecord> {

    void addLikeRecord(LikeRecordFormDTO recordDTO);

    Set<Long> isBizLiked(List<Long> bizIds);

    void readLikedTimesAndSendMessage(String bizType, int maxBizSize);
}

package com.aicomic.liked.task;

import com.aicomic.common.constants.LikeConstants;
import com.aicomic.liked.service.ILikeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LikeTimesCheckTask {

    private static final List<String> BIZ_TYPES = List.of(
            LikeConstants.BIZ_TYPE_COMMENT,
            LikeConstants.BIZ_TYPE_COMMENT_REPLY,
            LikeConstants.BIZ_TYPE_NOTE);
    private static final int MAX_BIZ_SIZE = 30;

    private final ILikeRecordService recordService;

    @Scheduled(fixedDelay = 20000)
    public void checkLikedTimes(){
        for (String bizType : BIZ_TYPES) {
            recordService.readLikedTimesAndSendMessage(bizType, MAX_BIZ_SIZE);
        }
    }
}

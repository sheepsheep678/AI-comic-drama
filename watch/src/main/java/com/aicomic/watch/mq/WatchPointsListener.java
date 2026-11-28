package com.aicomic.watch.mq;

import com.aicomic.common.autoconfigure.mq.AbstractRocketMqListener;
import com.aicomic.watch.enums.PointsRecordType;
import com.aicomic.watch.mq.message.SignInMessage;
import com.aicomic.watch.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import static com.aicomic.common.constants.MqConstants.ConsumerGroup.WATCH_POINTS_GROUP;
import static com.aicomic.common.constants.MqConstants.Tag.NOTE_GATHERED_TAG;
import static com.aicomic.common.constants.MqConstants.Tag.SIGN_IN_TAG;
import static com.aicomic.common.constants.MqConstants.Tag.WATCH_CLIP_TAG;
import static com.aicomic.common.constants.MqConstants.Tag.WRITE_NOTE_TAG;
import static com.aicomic.common.constants.MqConstants.Tag.WRITE_REPLY_TAG;
import static com.aicomic.common.constants.MqConstants.Topic.WATCH_TOPIC;

@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = WATCH_TOPIC,
        selectorExpression = WRITE_REPLY_TAG + " || " + SIGN_IN_TAG + " || " + WATCH_CLIP_TAG
                + " || " + WRITE_NOTE_TAG + " || " + NOTE_GATHERED_TAG,
        consumerGroup = WATCH_POINTS_GROUP
)
public class WatchPointsListener extends AbstractRocketMqListener {

    private final IPointsRecordService recordService;

    @Override
    protected void handleMessage(MessageExt message) {
        String tag = message.getTags();
        if (WRITE_REPLY_TAG.equals(tag)) {
            // 写回答
            recordService.addPointsRecord(readMessage(message, Long.class), 5, PointsRecordType.QA);
        } else if (SIGN_IN_TAG.equals(tag)) {
            // 签到
            SignInMessage signIn = readMessage(message, SignInMessage.class);
            recordService.addPointsRecord(signIn.getUserId(), signIn.getPoints(), PointsRecordType.SIGN);
        } else if (WATCH_CLIP_TAG.equals(tag)) {
            // 观看视频
            recordService.addPointsRecord(readMessage(message, Long.class), 10, PointsRecordType.WATCHING);
        } else if (WRITE_NOTE_TAG.equals(tag)) {
            // 写笔记
            recordService.addPointsRecord(readMessage(message, Long.class), 3, PointsRecordType.NOTE);
        } else if (NOTE_GATHERED_TAG.equals(tag)) {
            // 笔记被采集
            recordService.addPointsRecord(readMessage(message, Long.class), 2, PointsRecordType.NOTE);
        }
    }
}

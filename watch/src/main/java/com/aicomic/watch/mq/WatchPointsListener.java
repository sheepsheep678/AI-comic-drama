package com.aicomic.watch.mq;

import com.aicomic.common.constants.MqConstants;
import com.aicomic.watch.enums.PointsRecordType;
import com.aicomic.watch.mq.message.SignInMessage;
import com.aicomic.watch.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WatchPointsListener {

    private final IPointsRecordService recordService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "qa.points.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.WATCH_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.WRITE_REPLY
    ))
    public void listenWriteReplyMessage(Long userId){
        recordService.addPointsRecord(userId, 5, PointsRecordType.QA);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sign.points.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.WATCH_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.SIGN_IN
    ))
    public void listenSignInMessage(SignInMessage message){
        recordService.addPointsRecord(message.getUserId(), message.getPoints(), PointsRecordType.SIGN);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "watch.points.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.WATCH_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.WATCH_CLIP
    ))
    public void listenLearnSectionMessage(Long userId){
        recordService.addPointsRecord(userId, 10, PointsRecordType.WATCHING);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "note.new.points.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.WATCH_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.WRITE_NOTE
    ))
    public void listenWriteNodeMessage(Long userId){
        recordService.addPointsRecord(userId, 3, PointsRecordType.NOTE);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "note.gathered.points.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.WATCH_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.NOTE_GATHERED
    ))
    public void listenNodeGatheredMessage(Long userId){
        recordService.addPointsRecord(userId, 2, PointsRecordType.NOTE);
    }
}

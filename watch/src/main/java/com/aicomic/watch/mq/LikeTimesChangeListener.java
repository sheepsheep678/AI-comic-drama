package com.aicomic.watch.mq;

import com.aicomic.api.dto.comment.LikeTimesDTO;
import com.aicomic.watch.domain.po.InteractionReply;
import com.aicomic.watch.service.IHotReplyService;
import com.aicomic.watch.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.aicomic.common.constants.MqConstants.Exchange.LIKE_RECORD_EXCHANGE;
import static com.aicomic.common.constants.MqConstants.Key.QA_LIKE_TIMES_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeTimesChangeListener {

    private final IInteractionReplyService replyService;
    private final IHotReplyService hotReplyService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "qa.liked.times.queue", durable = "true"),
            exchange = @Exchange(name = LIKE_RECORD_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = QA_LIKE_TIMES_KEY
    ))
    public void listenReplyLikedTimesChange(List<LikeTimesDTO> likedTimesDTOs) {
        log.debug("监听到回答或评论的点赞数变更");
        if (likedTimesDTOs == null || likedTimesDTOs.isEmpty()) {
            return;
        }
        // 1.按 bizId 去重，保留最后一条绝对点赞数
        Map<Long, Integer> likedTimesMap = new HashMap<>(likedTimesDTOs.size());
        for (LikeTimesDTO dto : likedTimesDTOs) {
            if (dto != null && dto.getBizId() != null && dto.getLikedTimes() != null) {
                likedTimesMap.put(dto.getBizId(), dto.getLikedTimes());
            }
        }
        if (likedTimesMap.isEmpty()) {
            return;
        }
        // 2.查出回答行，拿到所属问题id并判断是否顶层回答
        List<InteractionReply> replies = replyService.listByIds(likedTimesMap.keySet());
        if (replies == null || replies.isEmpty()) {
            return;
        }
        List<InteractionReply> updates = new ArrayList<>(replies.size());
        Set<Long> questionIds = new HashSet<>();
        for (InteractionReply reply : replies) {
            updates.add(new InteractionReply()
                    .setId(reply.getId())
                    .setLikedTimes(likedTimesMap.get(reply.getId())));
            // 只有顶层回答的点赞会改变问题热度；子评论点赞只更新自身计数
            if ((reply.getAnswerId() == null || reply.getAnswerId() == 0L)
                    && reply.getQuestionId() != null) {
                questionIds.add(reply.getQuestionId());
            }
        }
        // 3.批量更新点赞数
        replyService.updateBatchById(updates);
        // 4.标记问题待重算；Redis 不可用时置空 DB 字段，让读路径实时兜底
        if (!questionIds.isEmpty() && !hotReplyService.markDirty(questionIds)) {
            hotReplyService.invalidate(questionIds);
        }
    }
}

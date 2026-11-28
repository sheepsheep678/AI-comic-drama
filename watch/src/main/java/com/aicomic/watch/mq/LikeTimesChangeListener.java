package com.aicomic.watch.mq;

import com.aicomic.api.dto.comment.LikeTimesDTO;
import com.aicomic.watch.domain.po.Comment;
import com.aicomic.watch.domain.po.CommentReply;
import com.aicomic.watch.service.ICommentReplyService;
import com.aicomic.watch.service.ICommentService;
import com.aicomic.watch.service.IHotReplyService;
import com.aicomic.common.autoconfigure.mq.AbstractRocketMqListener;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.aicomic.common.constants.MqConstants.ConsumerGroup.COMMENT_LIKE_TIMES_CHANGE_GROUP;
import static com.aicomic.common.constants.MqConstants.Tag.COMMENT_LIKE_TIMES_TAG;
import static com.aicomic.common.constants.MqConstants.Tag.COMMENT_REPLY_LIKE_TIMES_TAG;
import static com.aicomic.common.constants.MqConstants.Topic.LIKE_RECORD_TOPIC;

/**
 * 监听一级/二级评论的点赞数变更
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = LIKE_RECORD_TOPIC,
        selectorExpression = COMMENT_LIKE_TIMES_TAG + " || " + COMMENT_REPLY_LIKE_TIMES_TAG,
        consumerGroup = COMMENT_LIKE_TIMES_CHANGE_GROUP
)
public class LikeTimesChangeListener extends AbstractRocketMqListener {

    private final ICommentService commentService;
    private final ICommentReplyService replyService;
    private final IHotReplyService hotReplyService;

    @Override
    protected void handleMessage(MessageExt message) {
        List<LikeTimesDTO> likedTimesDTOs = readMessage(message, new TypeReference<List<LikeTimesDTO>>() {
        });
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
        if (COMMENT_LIKE_TIMES_TAG.equals(message.getTags())) {
            updateCommentLikedTimes(likedTimesMap);
        } else if (COMMENT_REPLY_LIKE_TIMES_TAG.equals(message.getTags())) {
            updateReplyLikedTimes(likedTimesMap);
        }
    }

    /**
     * 一级评论点赞数变更，只更新自身计数
     */
    private void updateCommentLikedTimes(Map<Long, Integer> likedTimesMap) {
        List<Comment> comments = commentService.listByIds(likedTimesMap.keySet());
        if (comments == null || comments.isEmpty()) {
            return;
        }
        List<Comment> updates = new ArrayList<>(comments.size());
        for (Comment comment : comments) {
            updates.add(new Comment()
                    .setId(comment.getId())
                    .setLikedTimes(likedTimesMap.get(comment.getId())));
        }
        commentService.updateBatchById(updates);
    }

    /**
     * 二级评论点赞数变更，更新自身计数并触发所属一级评论的热门重算
     */
    private void updateReplyLikedTimes(Map<Long, Integer> likedTimesMap) {
        List<CommentReply> replies = replyService.listByIds(likedTimesMap.keySet());
        if (replies == null || replies.isEmpty()) {
            return;
        }
        List<CommentReply> updates = new ArrayList<>(replies.size());
        Set<Long> commentIds = new HashSet<>();
        for (CommentReply reply : replies) {
            updates.add(new CommentReply()
                    .setId(reply.getId())
                    .setLikedTimes(likedTimesMap.get(reply.getId())));
            if (reply.getCommentId() != null) {
                commentIds.add(reply.getCommentId());
            }
        }
        replyService.updateBatchById(updates);
        // Redis 不可用时置空 DB 字段，让读路径实时兜底
        if (!commentIds.isEmpty() && !hotReplyService.markDirty(commentIds)) {
            hotReplyService.invalidate(commentIds);
        }
    }
}

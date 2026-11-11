package com.aicomic.watch.domain.dto;

import com.aicomic.watch.domain.po.InteractionReply;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 参与热度排名的回答候选
 */
@Data
@Accessors(chain = true)
public class HotReplyCandidate {

    /**
     * 回答id
     */
    private Long replyId;
    /**
     * 所属问题id
     */
    private Long questionId;
    /**
     * 回答者id
     */
    private Long userId;
    /**
     * 回答内容
     */
    private String content;
    /**
     * 是否匿名
     */
    private Boolean anonymity;
    /**
     * 点赞数
     */
    private Integer likedTimes;
    /**
     * 回复数
     */
    private Integer replyTimes;
    /**
     * 回答时间
     */
    private LocalDateTime createTime;
    /**
     * 是否隐藏
     */
    private Boolean hidden;

    public static HotReplyCandidate from(InteractionReply reply) {
        if (reply == null) {
            return null;
        }
        return new HotReplyCandidate()
                .setReplyId(reply.getId())
                .setQuestionId(reply.getQuestionId())
                .setUserId(reply.getUserId())
                .setContent(reply.getContent())
                .setAnonymity(reply.getAnonymity())
                .setLikedTimes(reply.getLikedTimes())
                .setReplyTimes(reply.getReplyTimes())
                .setCreateTime(reply.getCreateTime())
                .setHidden(reply.getHidden());
    }
}

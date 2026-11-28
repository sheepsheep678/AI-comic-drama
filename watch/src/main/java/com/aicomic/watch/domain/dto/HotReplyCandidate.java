package com.aicomic.watch.domain.dto;

import com.aicomic.watch.domain.po.CommentReply;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 参与热度排名的二级评论候选
 */
@Data
@Accessors(chain = true)
public class HotReplyCandidate {

    /**
     * 二级评论id
     */
    private Long replyId;
    /**
     * 所属一级评论id
     */
    private Long commentId;
    /**
     * 评论者id
     */
    private Long userId;
    /**
     * 评论内容
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
     * 评论时间
     */
    private LocalDateTime createTime;
    /**
     * 是否隐藏
     */
    private Boolean hidden;

    public static HotReplyCandidate from(CommentReply reply) {
        if (reply == null) {
            return null;
        }
        return new HotReplyCandidate()
                .setReplyId(reply.getId())
                .setCommentId(reply.getCommentId())
                .setUserId(reply.getUserId())
                .setContent(reply.getContent())
                .setAnonymity(reply.getAnonymity())
                .setLikedTimes(reply.getLikedTimes())
                .setCreateTime(reply.getCreateTime())
                .setHidden(reply.getHidden());
    }
}

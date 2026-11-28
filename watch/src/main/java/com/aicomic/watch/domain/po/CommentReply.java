package com.aicomic.watch.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 二级评论（严格两级，不再有子评论）
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comment_reply")
public class CommentReply implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 二级评论id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属一级评论id
     */
    private Long commentId;

    /**
     * 评论用户id
     */
    private Long userId;

    /**
     * 评论正文
     */
    private String content;

    /**
     * 被@的用户id
     */
    private Long targetUserId;

    /**
     * 被回复的二级评论id
     */
    private Long targetReplyId;

    /**
     * 点赞数量
     */
    private Integer likedTimes;

    /**
     * 是否匿名，默认false
     */
    private Boolean anonymity;

    /**
     * 是否被隐藏，默认false
     */
    private Boolean hidden;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

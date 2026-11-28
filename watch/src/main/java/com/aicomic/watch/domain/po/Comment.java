package com.aicomic.watch.domain.po;

import com.aicomic.watch.enums.CommentStatus;
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
 * 一级评论
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("`comment`")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 一级评论id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 评论正文
     */
    private String content;

    /**
     * 所属漫剧id
     */
    private Long courseId;

    /**
     * 所属章id
     */
    private Long chapterId;

    /**
     * 所属片段id
     */
    private Long sectionId;

    /**
     * 评论用户id
     */
    private Long userId;

    /**
     * 最新的一个二级评论id
     */
    private Long latestReplyId;

    /**
     * 点赞最高的前4个二级评论id，逗号分隔；null-未计算，空串-已计算但无可见回复
     */
    private String topReplyIds;

    /**
     * 二级评论数量（历史总数，隐藏不扣减）
     */
    private Integer replyTimes;

    /**
     * 一级评论点赞数
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
     * 管理端状态：0-未查看，1-已查看
     */
    private CommentStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

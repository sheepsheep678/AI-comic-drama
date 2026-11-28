package com.aicomic.watch.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "二级评论信息")
public class CommentReplyVO {

    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("所属一级评论id")
    private Long commentId;
    @ApiModelProperty("评论内容")
    private String content;
    @ApiModelProperty("是否匿名")
    private Boolean anonymity;
    @ApiModelProperty("是否隐藏")
    private Boolean hidden;
    @ApiModelProperty(value = "创建时间", example = "2026-11-28 19:52:36")
    private LocalDateTime createTime;

    @ApiModelProperty("当前评论者id，匿名时不返回")
    private Long userId;
    @ApiModelProperty("当前评论者昵称，匿名时不返回")
    private String userName;
    @ApiModelProperty("当前评论者头像，匿名时不返回")
    private String userIcon;
    @ApiModelProperty("当前评论者类型，2-观众，其它-创作者")
    private Integer userType;

    @ApiModelProperty("是否点过赞")
    private Boolean liked;
    @ApiModelProperty("点赞数量")
    private Integer likedTimes;

    @ApiModelProperty("被回复的二级评论id")
    private Long targetReplyId;
    @ApiModelProperty("目标用户id，匿名目标不返回")
    private Long targetUserId;
    @ApiModelProperty("目标用户昵称，匿名目标不返回")
    private String targetUserName;
}

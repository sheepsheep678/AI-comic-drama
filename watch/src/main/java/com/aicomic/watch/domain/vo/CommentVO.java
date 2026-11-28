package com.aicomic.watch.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "用户端一级评论信息")
public class CommentVO {

    @ApiModelProperty("主键id")
    private Long id;
    @ApiModelProperty("评论内容")
    private String content;
    @ApiModelProperty("二级评论数量")
    private Integer replyTimes;
    @ApiModelProperty("一级评论点赞数")
    private Integer likedTimes;
    @ApiModelProperty("当前用户是否点过赞")
    private Boolean liked;
    @ApiModelProperty(value = "创建时间", example = "2026-11-28 19:52:36")
    private LocalDateTime createTime;
    @ApiModelProperty("是否匿名评论")
    private Boolean anonymity;
    @ApiModelProperty("评论者id，匿名时不返回")
    private Long userId;
    @ApiModelProperty("评论者昵称，匿名时不返回")
    private String userName;
    @ApiModelProperty("评论者头像，匿名时不返回")
    private String userIcon;
    @ApiModelProperty("热度最高的二级评论列表")
    private List<HotReplyVO> hotReplies;
}

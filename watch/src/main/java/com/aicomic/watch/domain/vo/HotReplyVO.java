package com.aicomic.watch.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "一级评论下的热度二级评论")
public class HotReplyVO {

    @ApiModelProperty("二级评论id")
    private Long id;
    @ApiModelProperty("评论内容")
    private String content;
    @ApiModelProperty("是否匿名")
    private Boolean anonymity;
    @ApiModelProperty("评论者id，匿名时不返回")
    private Long userId;
    @ApiModelProperty("评论者昵称，匿名时不返回")
    private String userName;
    @ApiModelProperty("评论者头像，匿名时不返回")
    private String userIcon;
    @ApiModelProperty("点赞数")
    private Integer likedTimes;
    @ApiModelProperty(value = "评论时间", example = "2026-11-28 19:52:36")
    private LocalDateTime createTime;
    @ApiModelProperty("是否隐藏")
    private Boolean hidden;
}

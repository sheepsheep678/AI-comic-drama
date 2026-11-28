package com.aicomic.watch.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "管理端一级评论信息")
public class CommentAdminVO {

    @ApiModelProperty("主键id")
    private Long id;
    @ApiModelProperty("评论内容")
    private String content;
    @ApiModelProperty("二级评论数量")
    private Integer replyTimes;
    @ApiModelProperty("一级评论点赞数")
    private Integer likedTimes;
    @ApiModelProperty(value = "创建时间", example = "2026-11-28 19:52:36")
    private LocalDateTime createTime;
    @ApiModelProperty("管理端状态：0-未查看，1-已查看")
    private Integer status;
    @ApiModelProperty("是否匿名")
    private Boolean anonymity;
    @ApiModelProperty("是否被隐藏")
    private Boolean hidden;

    @ApiModelProperty("评论者昵称")
    private String userName;
    @ApiModelProperty("评论者头像")
    private String userIcon;
    @ApiModelProperty("漫剧名称")
    private String courseName;
    @ApiModelProperty("创作者名称")
    private String creatorName;
    @ApiModelProperty("章名称")
    private String chapterName;
    @ApiModelProperty("片段名称")
    private String sectionName;
    @ApiModelProperty("三级分类名称，中间使用/隔开")
    private String categoryName;
    @ApiModelProperty("热度最高的二级评论列表")
    private List<HotReplyVO> hotReplies;
}

package com.aicomic.watch.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "二级评论表单信息")
public class CommentReplyFormDTO {

    @ApiModelProperty("一级评论id")
    @NotNull(message = "一级评论id不能为空")
    private Long commentId;

    @ApiModelProperty("评论内容")
    @NotBlank(message = "评论内容不能为空")
    @Length(max = 1000, message = "评论内容长度不能超过1000")
    private String content;

    @ApiModelProperty("是否匿名评论")
    private Boolean anonymity;

    @ApiModelProperty("被回复的二级评论id，没有可不填")
    private Long targetReplyId;

    @ApiModelProperty("标记是否是观众提交的评论，默认true")
    private Boolean isViewer = true;
}

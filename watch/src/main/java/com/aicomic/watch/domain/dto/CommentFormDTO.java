package com.aicomic.watch.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "一级评论表单信息")
public class CommentFormDTO {

    @ApiModelProperty("漫剧id")
    @NotNull(message = "漫剧id不能为空")
    private Long courseId;

    @ApiModelProperty("章id")
    @NotNull(message = "章id不能为空")
    private Long chapterId;

    @ApiModelProperty("片段id")
    @NotNull(message = "片段id不能为空")
    private Long sectionId;

    @ApiModelProperty("评论内容")
    @NotBlank(message = "评论内容不能为空")
    @Length(max = 1000, message = "评论内容长度不能超过1000")
    private String content;

    @ApiModelProperty("是否匿名评论")
    private Boolean anonymity;
}

package com.aicomic.watch.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "互动问题表单信息")
public class QuestionFormDTO {
    @ApiModelProperty("漫剧id")
    @NotNull(message = "漫剧id不能为空")
    private Long courseId;
    @ApiModelProperty("章id")
    @NotNull(message = "章id不能为空")
    private Long chapterId;
    @ApiModelProperty("片段id")
    @NotNull(message = "片段id不能为空")
    private Long sectionId;
    @ApiModelProperty("标题")
    @NotNull(message = "标题不能为空")
    @Length(min = 1, max = 254, message = "标题长度太长")
    private String title;
    @ApiModelProperty("互动问题描述")
    @NotNull(message = "问题描述不能为空")
    private String description;
    @ApiModelProperty("是否匿名提问")
    private Boolean anonymity;
}

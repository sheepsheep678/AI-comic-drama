package com.aicomic.watch.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "修改一级评论表单信息")
public class CommentUpdateDTO {

    @ApiModelProperty("评论内容")
    @NotBlank(message = "评论内容不能为空")
    @Length(max = 1000, message = "评论内容长度不能超过1000")
    private String content;

    @ApiModelProperty("是否匿名评论")
    private Boolean anonymity;
}

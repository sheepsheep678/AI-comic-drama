package com.aicomic.watch.domain.query;

import com.aicomic.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "二级评论分页查询条件")
public class CommentReplyPageQuery extends PageQuery {

    @ApiModelProperty(value = "一级评论id", example = "1")
    @NotNull(message = "一级评论id不能为空")
    private Long commentId;
}

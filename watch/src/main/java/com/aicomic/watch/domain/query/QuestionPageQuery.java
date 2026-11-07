package com.aicomic.watch.domain.query;

import com.aicomic.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "互动问题分页查询条件")
public class QuestionPageQuery extends PageQuery {
    // 用户端查询条件
    @ApiModelProperty(value = "漫剧id")
    private Long courseId;
    @ApiModelProperty(value = "片段id", example = "1")
    private Long sectionId;
    @ApiModelProperty(value = "是否只查询我的问题", example = "1")
    private Boolean onlyMine = false;
}

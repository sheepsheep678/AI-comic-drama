package com.aicomic.watch.domain.query;

import com.aicomic.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "观看笔记分页查询条件")
public class NotePageQuery extends PageQuery {
    // 用户端查询条件
    @ApiModelProperty(value = "漫剧id")
    private Long courseId;
    @ApiModelProperty(value = "片段id", example = "1")
    private Long sectionId;
    @ApiModelProperty(value = "是否只查询我的笔记", example = "1")
    private Boolean onlyMine = false;
}

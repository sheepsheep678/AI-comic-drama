package com.aicomic.watch.domain.query;

import com.aicomic.common.domain.query.PageQuery;
import com.aicomic.common.utils.DateUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "一级评论管理端分页查询条件")
public class CommentAdminPageQuery extends PageQuery {

    @ApiModelProperty(value = "漫剧名称搜索关键字", example = "Redis")
    private String courseName;

    @ApiModelProperty(value = "管理端状态：0-未查看，1-已查看", example = "1")
    private Integer status;

    @ApiModelProperty(value = "创建时间区间的开始时间", example = "2026-11-28 19:52:36")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime beginTime;

    @ApiModelProperty(value = "创建时间区间的结束时间", example = "2026-11-28 19:52:36")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMAT)
    private LocalDateTime endTime;
}

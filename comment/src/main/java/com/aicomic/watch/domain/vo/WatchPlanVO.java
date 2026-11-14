package com.aicomic.watch.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "漫剧计划信息")
public class WatchPlanVO {

    @ApiModelProperty("主键lessonId")
    private Long id;

    @ApiModelProperty("漫剧id")
    private Long courseId;

    @ApiModelProperty("漫剧名称")
    private String courseName;

    @ApiModelProperty("每周计划观看章节数")
    private Integer weekFreq;

    @ApiModelProperty("漫剧章节数量")
    private Integer sections;

    @ApiModelProperty("本周已观看章节数")
    private Integer weekLearnedSections;

    @ApiModelProperty("总已观看章节数")
    private Integer learnedSections;

    @ApiModelProperty("最近一次观看时间")
    private LocalDateTime latestLearnTime;
}

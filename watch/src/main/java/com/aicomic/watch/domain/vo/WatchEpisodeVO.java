package com.aicomic.watch.domain.vo;

import com.aicomic.watch.enums.EpisodeStatus;
import com.aicomic.watch.enums.PlanStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "漫剧表信息")
public class WatchEpisodeVO {

    @ApiModelProperty("主键lessonId")
    private Long id;

    @ApiModelProperty("漫剧id")
    private Long courseId;

    @ApiModelProperty("漫剧名称")
    private String courseName;

    @ApiModelProperty("漫剧封面")
    private String courseCoverUrl;

    @ApiModelProperty("漫剧章节数量")
    private Integer sections;

    @ApiModelProperty("漫剧状态，0-未观看，1-观看中，2-已学完，3-已失效")
    private EpisodeStatus status;

    @ApiModelProperty("总已观看章节数")
    private Integer learnedSections;

    @ApiModelProperty("总已领取漫剧数")
    private Integer courseAmount;

    @ApiModelProperty("漫剧购买时间")
    private LocalDateTime createTime;

    @ApiModelProperty("漫剧过期时间，如果为null代表漫剧永久有效")
    private LocalDateTime expireTime;

    @ApiModelProperty("计划的观看频率")
    private Integer weekFreq;

    @ApiModelProperty("习计划状态，0-没有计划，1-计划进行中")
    private PlanStatus planStatus;

    @ApiModelProperty("最近观看的片段名")
    private String latestSectionName;

    @ApiModelProperty("最近观看的片段编号")
    private Integer latestSectionIndex;
}

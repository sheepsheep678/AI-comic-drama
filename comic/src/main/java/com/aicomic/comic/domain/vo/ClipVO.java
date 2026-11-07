package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "片段信息及观看进度")
public class ClipVO {
    @ApiModelProperty("对应章节的id")
    private Long id;
    @ApiModelProperty("对应章节的名称")
    private String name;
    @ApiModelProperty("片段序号")
    private Integer index;
    @ApiModelProperty("对应章节的类型，2-视频（片段），3-答题")
    private Integer type;
    @ApiModelProperty("视频总时长，单位秒")
    private Integer mediaDuration;
    @ApiModelProperty("素材id")
    private Long mediaId;
    @ApiModelProperty("是否支持免费试看")
    private Boolean trailer;
    @ApiModelProperty("题目数量")
    private Integer subjectNum;
    @ApiModelProperty("是否包含片段测试")
    private Boolean hasTest;
    @ApiModelProperty("视频的当前观看时长，单位秒")
    private Integer moment = 0;
    @ApiModelProperty("是否完成观看，默认false")
    private Boolean finished;

    public Boolean getHasTest() {
        return subjectNum != null && subjectNum > 0;
    }
}
package com.aicomic.api.dto.watch;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "片段信息及观看进度")
public class WatchRecordDTO {
    @ApiModelProperty("对应节的id")
    private Long sectionId;
    @ApiModelProperty("视频的当前观看时长，单位秒")
    private Integer moment;
    @ApiModelProperty("是否完成观看，默认false")
    private Boolean finished;
}

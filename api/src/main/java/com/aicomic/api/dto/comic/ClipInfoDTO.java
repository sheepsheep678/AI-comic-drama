package com.aicomic.api.dto.comic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel("片段信息，包含漫剧id和素材id")
@AllArgsConstructor
@NoArgsConstructor
public class ClipInfoDTO {
    @ApiModelProperty("漫剧id")
    private Long courseId;
    @ApiModelProperty("素材id")
    private Long mediaId;
    @ApiModelProperty("是否支持免费试看")
    private Boolean trailer;
    @ApiModelProperty("免费时长，不免费为0，单位分钟")
    private Integer freeDuration;
}

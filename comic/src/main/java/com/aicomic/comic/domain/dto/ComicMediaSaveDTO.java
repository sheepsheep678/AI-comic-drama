package com.aicomic.comic.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 漫剧视频保存模型
 * @since 2022/7/13 15:09
 * @version 1.0.0
 **/
@Data
@ApiModel(description = "漫剧视频保存模型")
public class ComicMediaSaveDTO {
    @ApiModelProperty("片段id")
    private Long cataId;
    @ApiModelProperty("素材id")
    private Long mediaId;
    @ApiModelProperty("是否支持试看")
    private Boolean trailer;
}

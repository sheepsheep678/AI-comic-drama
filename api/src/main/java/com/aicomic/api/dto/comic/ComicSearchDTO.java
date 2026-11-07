package com.aicomic.api.dto.comic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 漫剧信息
 * @ClassName ComicDTO
 * @since 2022/7/18 13:12
 * @version 1.0.0
 **/
@ApiModel(description = "漫剧信息")
@Data
public class ComicSearchDTO {
    @ApiModelProperty("漫剧id")
    private Long id;
    @ApiModelProperty("漫剧名称")
    private String name;
    @ApiModelProperty("一级漫剧分类id")
    private Long categoryIdLv1;
    @ApiModelProperty("二级漫剧分类id")
    private Long categoryIdLv2;
    @ApiModelProperty("三级漫剧分类id")
    private Long categoryIdLv3;
    @ApiModelProperty("漫剧封面")
    private String coverUrl;
    @ApiModelProperty("价格")
    private Integer price;
    @ApiModelProperty("是否免费")
    private Boolean free;
    @ApiModelProperty("发布时间")
    private LocalDateTime publishTime;
    @ApiModelProperty("章节数")
    private Integer sections;
    @ApiModelProperty("漫剧时长")
    private Integer duration;
    @ApiModelProperty("创作者id")
    private Long creator;
    @ApiModelProperty("漫剧类型，1：直播漫剧，2：录播漫剧")
    private Integer courseType;
    @ApiModelProperty(value = "漫剧领取人数（销量）", example = "3920")
    private Integer sold = 0;
    @ApiModelProperty(value = "漫剧评论得分，45代表4.5星", example = "35")
    private Integer score = 0;
}

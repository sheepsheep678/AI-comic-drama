package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 漫剧数据统计
 * @since 2022/7/10 15:36
 * @version 1.0.0
 **/
@Data
@ApiModel(description = "漫剧统计数据")
public class ComicStatisticsVO {
    @ApiModelProperty("漫剧总数量")
    private Integer totalNum;
    @ApiModelProperty("上架漫剧数量")
    private Integer onSaleNum;
    @ApiModelProperty("下架漫剧数量")
    private Integer offShelfNum;
    @ApiModelProperty("待上架漫剧数量")
    private Integer noSaleNum;
    @ApiModelProperty("完结漫剧数量")
    private Integer finishedNum;
    @ApiModelProperty("录播漫剧数量")
    private Integer recordNum;
    @ApiModelProperty("直播漫剧数")
    private Integer liveNum;

}

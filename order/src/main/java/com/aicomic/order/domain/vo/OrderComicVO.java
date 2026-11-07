package com.aicomic.order.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "订单中漫剧信息")
public class OrderComicVO {
    @ApiModelProperty("漫剧id")
    private Long id;
    @ApiModelProperty("漫剧名称")
    private String name;
    @ApiModelProperty("漫剧封面url")
    private String coverUrl;
    @ApiModelProperty("漫剧价格，单位元")
    private Integer price;
}

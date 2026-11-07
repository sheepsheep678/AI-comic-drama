package com.aicomic.order.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "购物车条目信息")
public class CartVO {
    @ApiModelProperty("购物车中条目id")
    private Long id;
    @ApiModelProperty("漫剧id")
    private Long courseId;
    @ApiModelProperty("漫剧名称")
    private String courseName;
    @ApiModelProperty("漫剧封面url")
    private String coverUrl;
    @ApiModelProperty("加入购物车时的漫剧价格，单位元")
    private Integer price;
    @ApiModelProperty("现在的漫剧价格，单位元")
    private Integer nowPrice;
    @ApiModelProperty("漫剧是否已经过期")
    private Boolean expired;
    @JsonIgnore
    @ApiModelProperty(value = "漫剧有效期", hidden = true)
    private LocalDateTime courseValidDate;
}
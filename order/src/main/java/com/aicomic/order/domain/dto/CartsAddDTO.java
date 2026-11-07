package com.aicomic.order.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "漫剧加入购物车")
public class CartsAddDTO {
    @ApiModelProperty("要加入购物车的漫剧id")
    @NotNull(message = "漫剧id不能为空")
    private Long courseId;
}
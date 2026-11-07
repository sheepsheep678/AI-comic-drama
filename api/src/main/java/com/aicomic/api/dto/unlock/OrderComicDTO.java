package com.aicomic.api.dto.unlock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(description = "订单中的漫剧信息")
public class OrderComicDTO {
    @ApiModelProperty("课id")
    private Long id;
    @ApiModelProperty("漫剧的三级分类id")
    private Long cateId;
    @ApiModelProperty("漫剧价格")
    private Integer price;
}

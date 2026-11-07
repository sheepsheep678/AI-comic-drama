package com.aicomic.comic.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 漫剧类目分页查询
 * @since 2022/7/10 11:21
 * @version 1.0.0
 **/
@ApiModel(description = "漫剧类目分页查询条件")
@Data
public class CategoryListDTO {
    @ApiModelProperty("类目状态1:正常，2：禁用")
    private Integer status;
    @ApiModelProperty("类目名称")
    private String name;


}

package com.aicomic.comic.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @since 2022/7/26 9:26
 * @version 1.0.0
 **/
@Data
public class ComicSimpleInfoListDTO {

    @ApiModelProperty("三级分类id列表")
    private List<Long> thirdCataIds;

    @ApiModelProperty("漫剧id列表")
    private List<Long> ids;
}

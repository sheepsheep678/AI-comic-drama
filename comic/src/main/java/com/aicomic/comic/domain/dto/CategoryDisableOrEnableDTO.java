package com.aicomic.comic.domain.dto;

import com.aicomic.common.validate.annotations.EnumValid;
import com.aicomic.comic.constants.ComicErrorInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 漫剧目录启用或停用模型
 *
 * @since 2022/7/10 15:24
 * @version 1.0.0
 **/
@Data
@ApiModel(description = "漫剧分类启用/禁用")
public class CategoryDisableOrEnableDTO {
    @ApiModelProperty("漫剧分类id")
    @NotNull(message = ComicErrorInfo.Msg.CATEGORY_ID_NOT_NULL)
    private Long id;
    @ApiModelProperty("漫剧分类状态，1：启用，0：禁用")
    @EnumValid(enumeration = {0,1}, message = ComicErrorInfo.Msg.CATEGORY_DISABLE_ENABLE_STATUS_ENUM)
    private Integer status;
}

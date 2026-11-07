package com.aicomic.comic.domain.dto;

import com.aicomic.comic.constants.ComicErrorInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @since 2022/7/20 16:50
 * @version 1.0.0
 **/
@ApiModel(description = "漫剧id")
@Data
public class ComicIdDTO {
    @ApiModelProperty("漫剧id")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_OPERATE_ID_NULL)
    private Long id;
}

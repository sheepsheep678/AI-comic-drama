package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @since 2022/7/13 11:26
 * @version 1.0.0
 **/
@Data
@ApiModel(description = "漫剧保存结果")
@AllArgsConstructor
@NotNull
@Builder
public class ComicSaveVO {
    @ApiModelProperty("漫剧id")
    private Long id;
}

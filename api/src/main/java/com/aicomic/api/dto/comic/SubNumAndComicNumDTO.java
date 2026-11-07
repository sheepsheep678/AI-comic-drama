package com.aicomic.api.dto.comic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创作者负责的漫剧数和出题数目的集合
 * @ClassName SubNumAndComicNumDTO
 * @since 2022/7/18 15:12
 * @version 1.0.0
 **/
@Data
@AllArgsConstructor
@NotNull
@ApiModel("创作者id和创作者对应的漫剧数，出题数")
public class SubNumAndComicNumDTO {
    @ApiModelProperty("创作者id")
    private Long teacherId;
    @ApiModelProperty("创作者负责的漫剧数")
    private Integer courseNum;
    @ApiModelProperty("创作者出题数")
    private Integer subjectNum;
}

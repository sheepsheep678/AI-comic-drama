package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 目录和习题模型
 * @since 2022/7/11 17:45
 * @version 1.0.0
 **/
@Data
@ApiModel(description = "漫剧题目统计")
public class EpisodeTagVO {
    @ApiModelProperty("片段或测试id")
    private Long cataId;
    @ApiModelProperty("片段或测试名称")
    private String cataName;
    @ApiModelProperty("类型，2：节，3：测试")
    private Integer type;
    @ApiModelProperty("题目数量")
    private Integer subjectNum;
    @ApiModelProperty("题目总分")
    private Integer subjectScore;
}

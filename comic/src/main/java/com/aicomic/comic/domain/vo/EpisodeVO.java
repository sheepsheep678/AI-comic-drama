package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName EpisodeVO
 * @Author 
 * @Date 2022/7/11 16:42
 * @Version
 **/
@Data
@ApiModel("漫剧目录")
public class EpisodeVO {
    @ApiModelProperty("章、节、练习id")
    private Long id;
    @ApiModelProperty("序号")
    private Integer index;
    @ApiModelProperty("章节练习名称")
    private String name;
    @ApiModelProperty("漫剧总时长,单位秒")
    private Integer mediaDuration;
    @ApiModelProperty("是否支持免费试看")
    private Boolean trailer;
    @ApiModelProperty("素材名称")
    private String mediaName;
    @ApiModelProperty("素材id")
    private Long mediaId;
    @ApiModelProperty("目录类型1：章，2：节，3：测试")
    private Integer type;
    @ApiModelProperty("题目数量")
    private Integer subjectNum;
    @ApiModelProperty("题目总分")
    private Integer totalScore;
    @ApiModelProperty("是否可以修改,默认不能修改")
    private Boolean canUpdate = false;
    @ApiModelProperty("该章的所有片段和练习")
    private List<EpisodeVO> sections;
    @ApiModelProperty("已上架最大序号，查看时值为空，编辑查看时片段必有值")
    private Integer maxIndexOnShelf;
    @ApiModelProperty("已上架片段最大序号，查看时，值为空，编辑查看时片段必有字段")
    private Integer maxSectionIndexOnShelf;
}

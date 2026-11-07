package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "漫剧和目录及观看进度信息")
public class ComicAndClipVO {
    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("漫剧名称")
    private String name;
    @ApiModelProperty("漫剧封面")
    private String coverUrl;
    @ApiModelProperty("漫剧章节数量")
    private Integer sections;
    @ApiModelProperty("创作者头像")
    private String creatorIcon;
    @ApiModelProperty("创作者名称")
    private String creatorName;
    @ApiModelProperty("id")
    private Long lessonId;
    @ApiModelProperty("正在观看的片段id")
    private Long latestSectionId;
    private List<ChapterVO> chapters;
}
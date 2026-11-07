package com.aicomic.search.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "漫剧信息")
public class ComicVO {
    @ApiModelProperty(value = "漫剧id", example = "1")
    private Long id;
    @ApiModelProperty(value = "漫剧名称", example = "Java")
    private String name;
    @ApiModelProperty(value = "漫剧价格，单位分", example = "32900")
    private Long price;
    @ApiModelProperty(value = "创作者名字", example = "张创作者")
    private String creator;
    @ApiModelProperty(value = "创作者头像", example = "default-user-icon.jpg")
    private String icon;
    @ApiModelProperty(value = "漫剧时长，单位秒", example = "3280")
    private Integer duration;
    @ApiModelProperty(value = "漫剧封面地址", example = "default-cover-url.jpg")
    private String coverUrl;
    @ApiModelProperty(value = "漫剧章节数量", example = "25")
    private Integer sections;
    @ApiModelProperty(value = "漫剧领取人数（销量）", example = "3920")
    private Integer sold;

    public static final String[] EXCLUDE_FIELDS =
            {"categoryIdLv1", "categoryIdLv2", "categoryIdLv3", "free",
                    "publishTime", "type", "status", "score"};
}

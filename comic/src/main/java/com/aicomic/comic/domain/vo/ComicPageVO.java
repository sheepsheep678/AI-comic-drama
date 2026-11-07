package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "漫剧信息")
public class ComicPageVO {
    @ApiModelProperty(value = "漫剧id", example = "1")
    private Long id;
    @ApiModelProperty(value = "漫剧名称", example = "Java")
    private String name;
    @ApiModelProperty(value = "漫剧价格，单位分", example = "32900")
    private Long price;
    @ApiModelProperty(value = "漫剧封面地址", example = "default-cover-url.jpg")
    private String coverUrl;
    @ApiModelProperty(value = "漫剧分类，三级分类，以/隔开")
    private String categories;
    @ApiModelProperty(value = "漫剧章节数量", example = "25")
    private Integer sections;
    @ApiModelProperty(value = "漫剧领取人数（销量）", example = "3920")
    private Integer sold;
    @ApiModelProperty(value = "漫剧评论得分，45代表4.5星", example = "35")
    private Integer score;
    @ApiModelProperty(value = "漫剧状态，1：待上架，2：已上架，3：已下架，4：已完结", example = "1")
    private Integer status;
    @ApiModelProperty(value = "更新人名字", example = "32900")
    private String updaterName;
    @ApiModelProperty(value = "更新时间", example = "2022-7-18 19:52:36")
    private LocalDateTime updateTime;
    @ApiModelProperty("漫剧编辑进度：1：基本信息已经保存，2：漫剧目录已经保存，3：漫剧视频已保存，4：漫剧题目已保存，5：漫剧创作者已经保存")
    private Integer step;
    @ApiModelProperty("漫剧发布时间")
    private LocalDateTime publishTime;
    @ApiModelProperty("下架时间")
    private LocalDateTime purchaseEndTime;

    public static final String[] EXCLUDE_FIELDS =
            {"free", "type", "creator","duration","publishTime"};
}

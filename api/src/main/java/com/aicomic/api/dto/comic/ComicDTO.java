package com.aicomic.api.dto.comic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("漫剧信息")
@Data
public class ComicDTO {
    @ApiModelProperty("漫剧id")
    private Long id;
    @ApiModelProperty("漫剧名称")
    private String name;
    @ApiModelProperty("一级漫剧分类id")
    private Long categoryIdLv1;
    @ApiModelProperty("二级漫剧分类id")
    private Long categoryIdLv2;
    @ApiModelProperty("三级漫剧分类id")
    private Long categoryIdLv3;
    @ApiModelProperty("漫剧封面")
    private String coverUrl;
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
    @ApiModelProperty("价格")
    private Integer price;
    @ApiModelProperty("视频播放时长")
    private Integer duration;
    @ApiModelProperty("漫剧有效期天数")
    private Integer validDuration;
    @ApiModelProperty("是否免费")
    private Boolean free;
    @ApiModelProperty("发布时间")
    private LocalDateTime publishTime;
    @ApiModelProperty("章节数")
    private Integer sections;
    @ApiModelProperty("漫剧状态")
    private  Byte status;
    @ApiModelProperty("创作者id")
    private Long creator;
    @ApiModelProperty("漫剧类型，1：直播漫剧，2：录播漫剧")
    private Integer courseType;
    @ApiModelProperty("更新时间")
    private Long updater;
    @ApiModelProperty("漫剧进行到的步骤，1：基本信息，2：目录，3：漫剧视频，4：漫剧题目，5：漫剧创作者")
    private Integer step;
    @ApiModelProperty(value = "漫剧领取人数（销量）", example = "3920")
    private Integer sold = 0;
    @ApiModelProperty(value = "漫剧评论得分，45代表4.5星", example = "35")
    private Integer score = 0;
    @ApiModelProperty("漫剧是否禁用,0:禁用，1：启用")
    private Integer enable;
}
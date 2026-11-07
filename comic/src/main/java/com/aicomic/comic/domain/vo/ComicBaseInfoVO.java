package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @since 2022/7/11 11:59
 * @version 1.0.0
 **/
@Data
@ApiModel(description = "漫剧基本信息")
public class ComicBaseInfoVO {
    @ApiModelProperty("漫剧id")
    private Long id;
    @ApiModelProperty("一级分类id")
    private Long firstCateId;
    @ApiModelProperty("二级分类id")
    private Long secondCateId;
    @ApiModelProperty("三级分类id")
    private Long thirdCateId;
    @ApiModelProperty("漫剧创建人")
    private String createrName;
    private Long creater;
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("封面url")
    private String coverUrl;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
    @ApiModelProperty("更新人名称")
    private String updaterName;
    private Long updater;
    @ApiModelProperty("课时总数量,去掉章，测试，用于编辑回显时该值为空")
    private Integer cataTotalNum;
    @ApiModelProperty("漫剧评分，用于编辑回显时该值为空")
    private Double coureScore = 0d;
    @ApiModelProperty("漫剧评分")
    private Integer score;
    @ApiModelProperty("领取人数，用于编辑回显时该值为空")
    private Integer purchaseNum = 0;
    @ApiModelProperty("观看人数，用于编辑回显时该值为空")
    private Integer studyNum = 0;
    @ApiModelProperty("退款人数，用于编辑回显时该值为空")
    private Integer refundNum = 0;
    @ApiModelProperty("实付总金额，用于编辑回显时该值为空")
    private Integer realPayAmount = 0;
    @ApiModelProperty("漫剧名称")
    private String name;
    @ApiModelProperty("漫剧分类名称，中间使用/隔开")
    private String cateNames;
    @ApiModelProperty("漫剧价格")
    private Integer price;
    @ApiModelProperty("购买有效期开始")
    private LocalDateTime purchaseStartTime;
    @ApiModelProperty
    private LocalDateTime purchaseEndTime;
    @ApiModelProperty("有效期")
    private Integer validDuration;
    @ApiModelProperty("漫剧介绍")
    private String introduce;
    @ApiModelProperty("使用人群")
    private String usePeople;
    @ApiModelProperty("详情")
    private String detail;
    //
    @ApiModelProperty("是否可以修改，默认不能修改")
    private Boolean canUpdate = false;
    @ApiModelProperty("是否免费")
    private Boolean free;
    @ApiModelProperty("步骤,1:已保存基本信息，2：已保存漫剧目录，3：已保存漫剧视频，4：已保存题目，5：已保存漫剧创作者")
    private Integer step;
    @ApiModelProperty("漫剧状态，1：待上架，2：已上架，3：下架，4：已完结")
    private Integer status;

}

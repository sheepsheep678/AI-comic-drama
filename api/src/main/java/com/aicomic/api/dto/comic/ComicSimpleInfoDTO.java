package com.aicomic.api.dto.comic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @since 2022/7/27 14:32
 * @version 1.0.0
 **/
@Data
public class ComicSimpleInfoDTO {
    @ApiModelProperty("漫剧id")
    private Long id;
    @ApiModelProperty("漫剧名称")
    private String name;
    @ApiModelProperty("封面url")
    private String coverUrl;
    @ApiModelProperty("价格")
    private Integer price;
    @ApiModelProperty("漫剧状态")
    private Integer status;
    @ApiModelProperty("是否是免费漫剧")
    private Boolean free;
    @ApiModelProperty("一级分类id")
    private Long firstCateId;
    @ApiModelProperty("二级分类id")
    private Long secondCateId;
    @ApiModelProperty("三级分类id")
    private Long thirdCateId;
    @ApiModelProperty("片段数量")
    private Integer sectionNum;
    @ApiModelProperty("漫剧购买有效期结束时间")
    private LocalDateTime purchaseEndTime;
    @ApiModelProperty("漫剧观看有效期，单位：月")
    private Integer validDuration;
    @JsonIgnore
    public List<Long> getCategoryIds(){
        return List.of(firstCateId, secondCateId, thirdCateId);
    }
}

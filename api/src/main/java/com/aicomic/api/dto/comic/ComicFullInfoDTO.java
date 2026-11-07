package com.aicomic.api.dto.comic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 漫剧信息
 *
 * @since 2022/8/5 16:54
 * @version 1.0.0
 **/
@Data
@ApiModel(description = "漫剧详细信息，包含漫剧、目录、创作者")
public class ComicFullInfoDTO {
    @ApiModelProperty("漫剧id")
    private Long id;
    @ApiModelProperty("漫剧名称")
    private String name;
    @ApiModelProperty("封面链接")
    private String coverUrl;
    @ApiModelProperty("价格")
    private Integer price;
    @ApiModelProperty("一级漫剧分类id")
    private Long firstCateId;
    @ApiModelProperty("二级漫剧分类id")
    private Long secondCateId;
    @ApiModelProperty("三级漫剧分类id")
    private Long thirdCateId;
    @ApiModelProperty("漫剧总节数")
    private Integer sectionNum;
    @ApiModelProperty("漫剧购买有效期结束时间")
    private LocalDateTime purchaseEndTime;
    @ApiModelProperty("漫剧观看有效期")
    private Integer validDuration;
    @ApiModelProperty("漫剧章信息")
    private List<EpisodeDTO> chapters;
    @ApiModelProperty("创作者列表")
    private List<Long> creatorIds;
    @JsonIgnore
    public List<Long> getCategoryIds(){
        return List.of(firstCateId, secondCateId, thirdCateId);
    }
}

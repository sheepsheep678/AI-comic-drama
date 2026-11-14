package com.aicomic.watch.domain.dto;

import com.aicomic.common.validate.annotations.EnumValid;
import com.aicomic.watch.enums.ClipType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "观看记录")
public class WatchRecordFormDTO {

    @ApiModelProperty("片段类型：1-视频，2-答题")
    @NotNull(message = "片段类型不能为空")
    @EnumValid(enumeration = {1, 2}, message = "片段类型错误，只能是：1-视频，2-答题")
    private ClipType sectionType;

    @ApiModelProperty("追剧列表id")
    @NotNull(message = "追剧列表id不能为空")
    private Long lessonId;

    @ApiModelProperty("对应节的id")
    @NotNull(message = "节的id不能为空")
    private Long sectionId;

    @ApiModelProperty("视频总时长，单位秒")
    private Integer duration;

    @ApiModelProperty("视频的当前观看时长，单位秒，第一次提交填0")
    private Integer moment;

    @ApiModelProperty("提交时间")
    private LocalDateTime commitTime;
}
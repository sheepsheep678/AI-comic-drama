package com.aicomic.api.dto.watch;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "观看追剧列表进度信息")
public class WatchEpisodeDTO {
    @ApiModelProperty("追剧列表id")
    private Long id;
    @ApiModelProperty("最近观看的片段id")
    private Long latestSectionId;
    @ApiModelProperty("观看过的片段的记录")
    private List<WatchRecordDTO> records;
}

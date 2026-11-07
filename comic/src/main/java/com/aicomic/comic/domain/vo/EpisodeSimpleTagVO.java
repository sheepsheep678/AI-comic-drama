package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @since 2022/8/15 16:04
 * @version 1.0.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpisodeSimpleTagVO {
    @ApiModelProperty("片段或练习id")
    private Long cataId;
    @ApiModelProperty("题目id")
    private List<TagInfo> tags;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TagInfo{
        private Long id;
        private String name;
    }
}

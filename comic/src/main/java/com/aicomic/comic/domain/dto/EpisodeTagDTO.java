package com.aicomic.comic.domain.dto;

import com.aicomic.comic.constants.ComicErrorInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 保存章节信息
 *
 * @since 2022/7/11 18:10
 * @version 1.0.0
 **/
@Data
@ApiModel(description = "片段、练习和题目关系模型")
public class EpisodeTagDTO {
        @ApiModelProperty("题目id")
        @NotNull(message = ComicErrorInfo.Msg.COMIC_TAG_SAVE_TAG_IDS_NULL)
        @Size(min = 1,message = ComicErrorInfo.Msg.COMIC_TAG_SAVE_TAG_IDS_NULL)
        private List<Long> subjectIds;
        @ApiModelProperty("片段或练习id")
        @NotNull(message = ComicErrorInfo.Msg.COMIC_TAG_SAVE_EPISODE_ID_NULL)
        private Long cataId;
}

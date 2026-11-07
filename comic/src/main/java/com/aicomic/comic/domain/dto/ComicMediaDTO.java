package com.aicomic.comic.domain.dto;

import com.aicomic.comic.constants.ComicErrorInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 漫剧视频模型
 *
 * @since 2022/7/11 17:12
 * @version 1.0.0
 **/
@Data
@ApiModel(description = "漫剧视频模型")
public class ComicMediaDTO {
    @ApiModelProperty("目录id")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_MEDIA_NULL)
    private Long cataId;
    @ApiModelProperty("素材id")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_MEDIA_NULL)
    private Long mediaId;
    @ApiModelProperty("是否支持试看")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_MEDIA_NULL)
    private Boolean trailer;
    @ApiModelProperty("素材名称")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_MEDIA_NULL)
    @Length(min = 1, message = ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_MEDIA_NULL)
    private String videoName;
    @ApiModelProperty("素材时长，单位s")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_MEDIA_NULL)
    @Min(value = 1, message = ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_MEDIA_NULL)
    private Integer mediaDuration;
}

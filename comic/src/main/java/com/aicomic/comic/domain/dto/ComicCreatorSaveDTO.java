package com.aicomic.comic.domain.dto;

import com.aicomic.comic.constants.ComicErrorInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 保存创作者漫剧关系
 * @ClassName ComicCreatorSaveDTO
 * @Author 
 * @Date 2022/7/13 14:59
 * @Version
 **/
@Data
@ApiModel("漫剧创作者关系模型")
public class ComicCreatorSaveDTO {
    @ApiModelProperty("漫剧id")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_CREATOR_SAVE_COMIC_ID_NULL)
    private Long id;
    @ApiModelProperty("创作者id和用户端是否展示，该列表按照界面上的顺序")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_CREATOR_SAVE_CREATORS_NULL)
//    @Min(value = 1, message = ComicErrorInfo.Msg.COMIC_CREATOR_SAVE_CREATORS_NULL)
    @Size(min = 1, max = 5, message = ComicErrorInfo.Msg.COMIC_CREATOR_SAVE_CREATORS_NUM_MAX )
    private List<CreatorInfo> creators;

    @Data
    @ApiModel("创作者id和用户端是否显示")
    public static class CreatorInfo{
        @ApiModelProperty("创作者id")
        @NotNull(message = ComicErrorInfo.Msg.COMIC_CREATOR_SAVE_CREATOR_ID_NULL)
        private Long id;
        @ApiModelProperty("用户端是否展示")
        @NotNull(message = ComicErrorInfo.Msg.COMIC_CREATOR_SAVE_CREATOR_SHOW)
        private Boolean isShow;
    }
}

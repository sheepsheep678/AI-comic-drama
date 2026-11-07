package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 漫剧相关创作者信息
 * @ClassName ComicCreatorVO
 * @Author 
 * @Date 2022/7/11 18:17
 * @Version
 **/
@Data
@ApiModel("创作者漫剧信息")
public class ComicCreatorVO {
    @ApiModelProperty("创作者漫剧关系id")
    private Long id;
    @ApiModelProperty("创作者头像")
    private String icon;
    @ApiModelProperty("形象照")
    private String photo;
    @ApiModelProperty("创作者姓名")
    private String name;
    @ApiModelProperty("创作者介绍")
    private String introduce;
    @ApiModelProperty("用户端是否显示")
    private Boolean isShow;
    @ApiModelProperty("职位")
    private String job;

}

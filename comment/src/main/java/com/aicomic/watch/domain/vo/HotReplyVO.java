package com.aicomic.watch.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "问题下的热度回答")
public class HotReplyVO {

    @ApiModelProperty("回答id")
    private Long id;
    @ApiModelProperty("回答内容")
    private String content;
    @ApiModelProperty("是否匿名")
    private Boolean anonymity;
    @ApiModelProperty("回答者id，匿名时不返回")
    private Long userId;
    @ApiModelProperty("回答者昵称，匿名时不返回")
    private String userName;
    @ApiModelProperty("回答者头像，匿名时不返回")
    private String userIcon;
    @ApiModelProperty("点赞数")
    private Integer likedTimes;
    @ApiModelProperty("回复数")
    private Integer replyTimes;
    @ApiModelProperty(value = "回答时间", example = "2022-7-18 19:52:36")
    private LocalDateTime createTime;
    @ApiModelProperty("是否隐藏")
    private Boolean hidden;
}

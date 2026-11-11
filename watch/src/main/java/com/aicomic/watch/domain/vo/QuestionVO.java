package com.aicomic.watch.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "用户端互动问题信息")
public class QuestionVO {
    @ApiModelProperty("主键id")
    private Long id;
    @ApiModelProperty("互动问题名称")
    private String title;
    @ApiModelProperty("互动问题描述")
    private String description;
    @ApiModelProperty("回答数量，0表示没有回答")
    private Integer answerTimes;
    @ApiModelProperty(value = "创建时间", example = "2022-7-18 19:52:36")
    private LocalDateTime createTime;
    @ApiModelProperty("是否匿名提问")
    private Boolean anonymity;
    @ApiModelProperty("提问者id")
    private Long userId;
    @ApiModelProperty("提问者昵称")
    private String userName;
    @ApiModelProperty("提问者头像")
    private String userIcon;
    @ApiModelProperty("热度最高的回答列表")
    private List<HotReplyVO> hotReplies;
}
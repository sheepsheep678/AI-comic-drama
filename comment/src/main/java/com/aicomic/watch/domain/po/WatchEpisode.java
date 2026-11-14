package com.aicomic.watch.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.aicomic.watch.enums.EpisodeStatus;
import com.aicomic.watch.enums.PlanStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 观众漫剧表
 * </p>
 *
 * @since 2022-12-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("watch_episode")
public class WatchEpisode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 观众id
     */
    private Long userId;

    /**
     * 漫剧id
     */
    private Long courseId;

    /**
     * 漫剧状态，0-未观看，1-观看中，2-已学完，3-已失效
     */
    private EpisodeStatus status;

    /**
     * 每周观看频率，每周3天，每天2节，则频率为6
     */
    private Integer weekFreq;

    /**
     * 观看计划状态，0-没有计划，1-计划进行中
     */
    private PlanStatus planStatus;

    /**
     * 已观看片段数量
     */
    private Integer learnedSections;

    /**
     * 最近一次观看的片段id
     */
    private Long latestSectionId;

    /**
     * 最近一次观看的时间
     */
    private LocalDateTime latestLearnTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}

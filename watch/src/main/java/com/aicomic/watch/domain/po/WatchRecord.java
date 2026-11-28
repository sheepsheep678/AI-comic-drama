package com.aicomic.watch.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 观看记录表
 * </p>
 *
 * @since 2022-12-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("watch_record")
public class WatchRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 观看记录的id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 对应追剧列表的id
     */
    private Long lessonId;

    /**
     * 对应片段的id
     */
    private Long sectionId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 视频的当前观看时间点，单位秒
     */
    private Integer moment;

    /**
     * 是否完成观看，默认false
     */
    private Boolean finished;

    /**
     * 第一次观看时间
     */
    private LocalDateTime createTime;

    /**
     * 完成观看的时间
     */
    private LocalDateTime finishTime;

    /**
     * 更新时间（最近一次观看时间）
     */
    private LocalDateTime updateTime;


}

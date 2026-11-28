package com.aicomic.watch.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.aicomic.watch.enums.PointsRecordType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 观看积分记录，每个月底清零
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("points_record")
public class PointsRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 积分记录表id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 积分方式：1-漫剧观看，2-每日签到，3-漫剧问答， 4-漫剧笔记，5-漫剧评论
     */
    private PointsRecordType type;

    /**
     * 积分值
     */
    private Integer points;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}

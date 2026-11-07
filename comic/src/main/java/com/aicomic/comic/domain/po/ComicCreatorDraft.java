package com.aicomic.comic.domain.po;

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
 * 漫剧创作者关系表草稿
 * </p>
 *
 * @since 2022-07-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comic_creator_draft")
public class ComicCreatorDraft implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 漫剧创作者关系id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 漫剧id
     */
    private Long courseId;

    /**
     * 创作者id
     */
    private Long teacherId;

    /**
     * 用户端是否展示
     */
    private Integer isShow;

    /**
     * 序号
     */
    private Integer cIndex;

    /**
     * 部门id
     */
    private Long depId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long creater;

    /**
     * 更新人
     */
    private Long updater;

    /**
     * 逻辑删除
     */
    private Integer deleted;


}

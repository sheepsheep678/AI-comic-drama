package com.aicomic.comic.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 目录草稿
 * </p>
 *
 * @since 2022-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comic_episode_draft")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComicEpisodeDraft implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 设置目录基本信息
     *
     * @param cIndex 目录序号
     * @param name 目录名称
     * @param type 类型
     * @param parentCatalogueId 父目录id
     * @param courseId 漫剧id
     */
    public void setCataBaseInfo(Integer cIndex, String name, Integer type, Long parentCatalogueId, Long courseId){
        this.cIndex = cIndex;
        this.name = name;
        this.type = type;
        this.parentCatalogueId = parentCatalogueId;
        this.courseId = courseId;
    }

    /**
     * 漫剧目录id
     */
    private Long id;

    /**
     * 目录名称
     */
    private String name;

    /**
     * 是否支持试看
     */
    private Integer trailer;

    /**
     * 漫剧id
     */
    private Long courseId;

    /**
     * 目录类型1：章节，2：片段，3：测试
     */
    private Integer type;

    /**
     * 所属章节id，只有片段和测试有该值，章节没有，章节默认为0
     */
    private Long parentCatalogueId;

    /**
     * 素材id
     */
    private Long mediaId;

    /**
     * 视频id
     */
    private Long videoId;

    /**
     * 视频名称
     */
    private String videoName;

    /**
     * 直播开始时间
     */
    private LocalDateTime livingStartTime;

    /**
     * 直播结束时间
     */
    private LocalDateTime livingEndTime;

    /**
     * 是否支持回放
     */
    private Integer playBack;

    /**
     * 用于章节排序
     */
    private Integer cIndex;

    /**
     * 以s为单位
     */
    private Integer mediaDuration;

    /**
     * 是否可以修改，上架后的目录位置不能移动
     */
    private Boolean canUpdate;
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
//    private Integer deleted;


}

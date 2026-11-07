package com.aicomic.comic.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 草稿漫剧
 * </p>
 *
 * @since 2022-07-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comic_draft")
public class ComicDraft implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 漫剧草稿id，对应正式草稿id
     */
    private Long id;

    /**
     * 漫剧名称
     */
    private String name;

    /**
     * 漫剧类型，1：直播课，2：录播课
     */
    private Integer courseType;

    /**
     * 封面链接
     */
    private String coverUrl;

    /**
     * 一级漫剧分类id
     */
    private Long firstCateId;

    /**
     * 二级漫剧分类id
     */
    private Long secondCateId;

    /**
     * 三级漫剧分类id
     */
    private Long thirdCateId;

    /**
     * 售卖方式0付费，1：免费
     */
    private Integer free;

    /**
     * 漫剧价格，单位为分
     */
    private Integer price;

    /**
     * 模板类型，1：固定模板，2：自定义模板
     */
    private Integer templateType;

    /**
     * 自定义模板的连接
     */
    private String templateUrl;

    /**
     * 漫剧状态，1：待上架，2：已上架，3：下架，4：已完结
     */
    private Integer status;

    /**
     * 漫剧购买有效期开始时间
     */
    private LocalDateTime purchaseStartTime;

    /**
     * 漫剧购买有效期结束时间
     */
    private LocalDateTime purchaseEndTime;

    /**
     * 信息填写进度1：基本信息已经保存，2：漫剧目录已经保存，3：漫剧视频已保存，4：漫剧题目已保存，5：漫剧创作者已经保存
     */
    private Integer step;

    /**
     * 漫剧评分，45代表4.5星
     */
    private Integer score;

    /**
     * 视频总时长
     */
    private Integer mediaDuration;

    /**
     * 漫剧有效期，单位月
     */
    private Integer validDuration;

    /**
     * 漫剧总节数
     */
    private Integer sectionNum;

    /**
     * 是否可以修改
     */
    private Boolean canUpdate;

    private Integer cVersion;
    /**
     * 部门id
     */
    private Long depId;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

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



}

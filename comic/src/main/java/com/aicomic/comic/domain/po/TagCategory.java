package com.aicomic.comic.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 漫剧分类关系表
 * </p>
 *
 * @since 2022-07-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tag_category")
public class TagCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 题目id
     */
    private Long subjectId;

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


}

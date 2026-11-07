package com.aicomic.comic.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 * 漫剧题目关系列表
 * </p>
 *
 * @since 2022-07-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comic_tag")
@NotNull
@AllArgsConstructor
public class ComicTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 漫剧题目关系id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long subjectId;


}

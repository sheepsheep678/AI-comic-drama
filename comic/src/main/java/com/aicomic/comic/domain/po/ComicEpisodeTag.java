package com.aicomic.comic.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 漫剧-题目关系表草稿
 * </p>
 *
 * @since 2022-07-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comic_episode_tag")
public class ComicEpisodeTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 片段题目关系id
     */
    private Long id;

    /**
     * 漫剧id
     */
    private Long courseId;

    /**
     * 片段id
     */
    private Long cataId;

    /**
     * 题目id
     */
    private Long subjectId;


}

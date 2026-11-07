package com.aicomic.comic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aicomic.comic.domain.po.ComicContentDraft;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 漫剧内容，主要是一些大文本 Mapper 接口
 * </p>
 *
 * @since 2022-07-18
 */
public interface ComicContentDraftMapper extends BaseMapper<ComicContentDraft> {

    String COLUMNS ="id,course_introduce,use_people,course_detail,dep_id,create_time,update_time,creater,updater,deleted";

    @Insert("insert into comic_content_draft (" + COLUMNS +
            ") (select " + COLUMNS + " from comic_content where id=#{id})")
    int insertFromCourseContent(@Param("id") Long id);

}

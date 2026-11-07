package com.aicomic.comic.mapper;

import com.aicomic.comic.domain.po.ComicCreatorDraft;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 漫剧创作者关系表草稿 Mapper 接口
 * </p>
 *
 * @since 2022-07-20
 */
public interface ComicCreatorDraftMapper extends BaseMapper<ComicCreatorDraft> {

    String COLUMNS = "id, course_id, teacher_id, is_show, c_index, dep_id, create_time, update_time, creater, updater, deleted";
    /**
     * 根据漫剧id删除漫剧中的创作者
     * @param courseId
     * @return
     */
    @Delete("delete from comic_creator_draft where course_id=#{courseId}")
    int deleteByCourseId(@Param("courseId") Long courseId);

    @Insert("insert into comic_creator_draft (" + COLUMNS + ") (select " + COLUMNS + " from comic_creator " +
            "where course_id= #{courseId} and deleted = 0)")
    int insertFromCourseTeacher(@Param("courseId") Long courseId);


}

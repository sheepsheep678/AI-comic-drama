package com.aicomic.comic.mapper;

import com.aicomic.comic.domain.po.ComicTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 漫剧题目关系列表 Mapper 接口
 * </p>
 *
 * @since 2022-07-17
 */
public interface ComicTagMapper extends BaseMapper<ComicTag> {

    @Insert("<script>insert into comic_tag (course_id,subject_id) " +
            "value <foreach collection='courseSubjects' item='cs' separator=','>(#{cs.courseId},#{cs.subjectId})</foreach></script>")
    int batchInsert(@Param("courseSubjects")List<ComicTag> courseSubjects);
}

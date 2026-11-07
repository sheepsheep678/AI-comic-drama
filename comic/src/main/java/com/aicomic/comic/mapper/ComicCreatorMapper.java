package com.aicomic.comic.mapper;

import com.aicomic.comic.domain.po.ComicCreator;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 漫剧创作者关系表草稿 Mapper 接口
 * </p>
 *
 * @since 2022-07-20
 */
public interface ComicCreatorMapper extends BaseMapper<ComicCreator> {

    @Delete("delete from comic_creator where course_id=#{courseId}")
    int deleteByCourseId(@Param("courseId") Long courseId);

}

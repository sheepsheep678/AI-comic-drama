package com.aicomic.comic.mapper;

import com.aicomic.comic.domain.po.EpisodeIdAndSubScore;
import com.aicomic.comic.domain.po.ComicEpisodeTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 漫剧-题目关系表草稿 Mapper 接口
 * </p>
 *
 * @since 2022-07-20
 */
public interface ComicEpisodeTagMapper extends BaseMapper<ComicEpisodeTag> {

    @Insert("<script>insert into comic_episode_tag (course_id,cata_id,subject_id) value " +
            "<foreach collection='courseCataSubjects' item='ccs' separator=','>" +
            "(#{ccs.courseId},#{ccs.cataId},#{ccs.subjectId})" +
            "</foreach></script>")
    int batchInsert(@Param("courseCataSubjects")List<ComicEpisodeTag> courseCataSubjects);

    @Delete("delete from comic_episode_tag where course_id=#{courseId}")
    int deleteByCourseId(@Param("courseId") Long courseId);

    @Select("select ccs.cata_id as cataId,ccs.subject_id as subjectId, s.score from comic_episode_tag ccs " +
            "left join tag s on ccs.subject_id=s.id where ccs.course_id=#{courseId}")
    List<EpisodeIdAndSubScore> queryCataIdAndScoreByCorseId(@Param("courseId") Long courseId);

    @Select("select subject_id from comic_episode_tag where cata_id=#{cataId}")
    List<Long> querySubjectIdByCataId(@Param("cataId") Long cataId);


}

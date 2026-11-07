package com.aicomic.comic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aicomic.comic.domain.po.EpisodeIdAndSubScore;
import com.aicomic.comic.domain.po.ComicEpisodeTagDraft;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>
 * 漫剧-题目关系表草稿 Mapper 接口
 * </p>
 *
 * @since 2022-07-20
 */
public interface ComicEpisodeTagDraftMapper extends BaseMapper<ComicEpisodeTagDraft> {

    @Insert("<script>insert into comic_episode_tag_draft(course_id,cata_id,subject_id) value " +
            "<foreach collection='pos' item='po' separator=','>(#{po.courseId},#{po.cataId},#{po.subjectId})</foreach></script>")
    int batchInsert(@Param("pos")List<ComicEpisodeTagDraft> courseCataSubjectDrafts);

    /**
     * 根据id删除漫剧对应的题目
     * @param courseId
     * @return
     */
    @Delete("delete from comic_episode_tag_draft where course_id=#{courseId}")
    int deleteByCourseId(@Param("courseId") Long courseId);

    @Select("select course_id,cata_id,subject_id from comic_episode_tag_draft where course_id=#{courseId}")
    @ResultMap("BaseResultMap")
    List<ComicEpisodeTagDraft> getByCourseId(@Param("courseId") Long courseId);

    @Insert("insert into comic_episode_tag_draft (course_id,cata_id,subject_id) " +
            "(select course_id,cata_id,subject_id from comic_episode_tag where course_id=#{courseId})")
    int insertFromComicEpisodeTag(@Param("courseId") Long courseId);

    @Select("select ccs.cata_id as cataId,ccs.subject_id as subjectId, s.score from comic_episode_tag_draft ccs " +
            "left join tag s on ccs.subject_id=s.id where ccs.course_id=#{courseId}")
    List<EpisodeIdAndSubScore> queryCataIdAndScoreByCorseId(@Param("courseId") Long courseId);

}

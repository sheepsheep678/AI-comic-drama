package com.aicomic.comic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aicomic.api.dto.IdAndNumDTO;
import com.aicomic.comic.domain.po.Category3PO;
import com.aicomic.comic.domain.po.Comic;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 草稿漫剧 Mapper 接口
 * </p>
 *
 * @since 2022-07-22
 */
public interface ComicMapper extends BaseMapper<Comic> {
    @Select("select count(1) from comic where name = #{name}")
    int countSameName(@Param("name") String name);

    int updateVariableById(@Param("po") Comic course);

    /**
     * 批量查询创作者所负责的漫剧数量
     * @param creatorIds
     * @return
     */
    @Select("<script>SELECT ct.teacher_id as id,count(*) as num " +
            " from comic c LEFT JOIN comic_creator ct on c.id=ct.course_id " +
            "where c.status!=1 and c.deleted=0 and ct.teacher_id in (<foreach collection='creatorIds' " +
            "item='teacherId' separator=','>#{teacherId}</foreach>)" +
            " GROUP BY ct.teacher_id</script>")
    List<IdAndNumDTO> countCourseNumOfTeacher(@Param("creatorIds")List<Long> creatorIds);

    @Select("select distinct first_cate_id as 'firstCateId',second_cate_id as 'secondCateId'," +
            "third_cate_id as 'thirdCateId' from comic where status=2")
    List<Category3PO> queryCategoryIdWithCourse();
}

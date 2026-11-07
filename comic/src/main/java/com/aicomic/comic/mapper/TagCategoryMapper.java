package com.aicomic.comic.mapper;

import com.aicomic.comic.domain.po.TagCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 漫剧分类关系表 Mapper 接口
 * </p>
 *
 * @since 2022-07-15
 */
public interface TagCategoryMapper extends BaseMapper<TagCategory> {

    @Insert("<script>INSERT INTO tag_category (subject_id,first_cate_id,second_cate_id,third_cate_id) " +
            "value <foreach collection='scs' item='sc'  separator=','>(#{sc.subjectId},#{sc.firstCateId}," +
            "#{sc.secondCateId},#{sc.thirdCateId})</foreach></script>")
    int batchInsert(@Param("scs") List<TagCategory> subjectCategoryList);

    /**
     * 获取指定漫剧分类下所有漫剧的数量
     *
     * @param categoryId
     * @param level
     * @return
     */
    @Select("<script>select count(*) from tag_category where 1= 1 <if test='level==1'> and first_cate_id=#{categoryId} </if> " +
            "<if test='level==2'> and second_cate_id=#{categoryId} </if>" +
            "<if test='level==3'> and third_cate_id=#{categoryId} </if>" +
            "</script>")
    int countSubjectNum(@Param("categoryId") Long categoryId, @Param("level") Integer level);

}

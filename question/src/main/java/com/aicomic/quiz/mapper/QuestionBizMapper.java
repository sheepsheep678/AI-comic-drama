package com.aicomic.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aicomic.api.dto.IdAndNumDTO;
import com.aicomic.quiz.domain.po.QuestionBiz;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 问题和业务关联表，例如把片段id和问题id关联，一个片段下可以有多个问题 Mapper 接口
 * </p>
 *
 * @since 2022-09-02
 */
public interface QuestionBizMapper extends BaseMapper<QuestionBiz> {

    List<IdAndNumDTO> countQuestionScoresByBizIds(@Param("bizIds") Iterable<Long> bizIds);

    List<IdAndNumDTO> countUsedTimes(@Param("qIds") Iterable<Long> qIds);
}

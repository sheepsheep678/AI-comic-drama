package com.aicomic.quiz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.api.dto.quiz.QuestionBizDTO;
import com.aicomic.quiz.domain.po.QuestionBiz;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 问题和业务关联表，例如把片段id和问题id关联，一个片段下可以有多个问题 服务类
 * </p>
 *
 * @since 2022-09-02
 */
public interface IQuestionBizService extends IService<QuestionBiz> {

    int countUsedTimes(Long questionId);

    Map<Long, Integer> countUsedTimes(Set<Long> qIds);

    List<QuestionBizDTO> queryQuestionIdsByBizId(Long bizId);

    List<QuestionBizDTO> queryQuestionIdsByBizIds(List<Long> bizIds);

    void saveQuestionBizInfoBatch(List<QuestionBizDTO> qbs);

    Map<Long, Integer> queryQuestionScoresByBizIds(Iterable<Long> bizIds);
}

package com.aicomic.quiz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.api.dto.quiz.QuestionDTO;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.quiz.domain.dto.QuestionFormDTO;
import com.aicomic.quiz.domain.po.Question;
import com.aicomic.quiz.domain.query.QuestionPageQuery;
import com.aicomic.quiz.domain.vo.QuestionDetailVO;
import com.aicomic.quiz.domain.vo.QuestionPageVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 题目 服务类
 * </p>
 *
 * @since 2022-09-02
 */
public interface IQuestionService extends IService<Question> {

    void addQuestion(QuestionFormDTO questionFormDTO);

    void updateQuestion(QuestionFormDTO questionDTO);

    void deleteQuestionById(Long id);

    PageDTO<QuestionPageVO> queryQuestionByPage(QuestionPageQuery query);

    QuestionDetailVO queryQuestionDetailById(Long id);

    List<QuestionDTO> queryQuestionByIds(List<Long> ids);

    Map<Long, Integer> countQuestionNumOfCreater(List<Long> createrIds);

    List<QuestionDTO> queryQuestionByBizId(Long bizId);

    Boolean checkNameValid(String name);

    Map<Long, Integer> queryQuestionScores(List<Long> ids);
}

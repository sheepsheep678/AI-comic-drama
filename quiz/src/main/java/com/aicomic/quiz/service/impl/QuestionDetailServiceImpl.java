package com.aicomic.quiz.service.impl;

import com.aicomic.quiz.domain.po.QuestionDetail;
import com.aicomic.quiz.mapper.QuestionDetailMapper;
import com.aicomic.quiz.service.IQuestionDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 题目 服务实现类
 * </p>
 *
 * @since 2022-09-02
 */
@Service
public class QuestionDetailServiceImpl extends ServiceImpl<QuestionDetailMapper, QuestionDetail> implements IQuestionDetailService {

}

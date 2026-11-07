package com.aicomic.watch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.domain.query.PageQuery;
import com.aicomic.watch.domain.po.WatchEpisode;
import com.aicomic.watch.domain.vo.WatchEpisodeVO;
import com.aicomic.watch.domain.vo.WatchPlanPageVO;

import java.util.List;

/**
 * <p>
 * 观众漫剧表 服务类
 * </p>
 *
 * @since 2022-12-02
 */
public interface IWatchEpisodeService extends IService<WatchEpisode> {

    void addUserLessons(Long userId, List<Long> courseIds);

    PageDTO<WatchEpisodeVO> queryMyLessons(PageQuery query);

    WatchEpisodeVO queryMyCurrentLesson();

    WatchEpisodeVO queryLessonByCourseId(Long courseId);

    void deleteCourseFromLesson(Long userId, Long courseId);

    Integer countWatchEpisodeByCourse(Long courseId);

    Long isLessonValid(Long courseId);

    WatchEpisode queryByUserAndCourseId(Long userId, Long courseId);

    void createWatchPlan(Long courseId, Integer freq);

    WatchPlanPageVO queryMyPlans(PageQuery query);
}

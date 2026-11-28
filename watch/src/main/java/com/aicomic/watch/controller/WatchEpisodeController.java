package com.aicomic.watch.controller;


import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.domain.query.PageQuery;
import com.aicomic.watch.domain.dto.WatchPlanDTO;
import com.aicomic.watch.domain.vo.WatchEpisodeVO;
import com.aicomic.watch.domain.vo.WatchPlanPageVO;
import com.aicomic.watch.service.IWatchEpisodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 观众漫剧表 前端控制器
 * </p>
 *
 * @since 2022-12-02
 */
@RestController
@RequestMapping("/episodes")
@Api(tags = "我的追剧列表相关接口")
@RequiredArgsConstructor
public class WatchEpisodeController {

    private final IWatchEpisodeService lessonService;

    @GetMapping("/page")
    @ApiOperation("分页查询我的追剧列表")
    public PageDTO<WatchEpisodeVO> queryMyLessons(PageQuery query) {
        return lessonService.queryMyLessons(query);
    }

    @GetMapping("/now")
    @ApiOperation("查询我正在观看的漫剧")
    public WatchEpisodeVO queryMyCurrentLesson() {
        return lessonService.queryMyCurrentLesson();
    }

    @GetMapping("/{courseId}")
    @ApiOperation("查询指定漫剧信息")
    public WatchEpisodeVO queryLessonByCourseId(
            @ApiParam(value = "漫剧id" ,example = "1") @PathVariable("courseId") Long courseId) {
        return lessonService.queryLessonByCourseId(courseId);
    }

    @DeleteMapping("/{courseId}")
    @ApiOperation("删除指定漫剧信息")
    public void deleteCourseFromLesson(
            @ApiParam(value = "漫剧id" ,example = "1") @PathVariable("courseId") Long courseId) {
        lessonService.deleteCourseFromLesson(null, courseId);
    }

    @ApiOperation("统计漫剧观看人数")
    @GetMapping("/{courseId}/count")
    public Integer countWatchEpisodeByCourse(
            @ApiParam(value = "漫剧id" ,example = "1") @PathVariable("courseId") Long courseId){
        return lessonService.countWatchEpisodeByCourse(courseId);
    }

    @ApiOperation("校验当前漫剧是否已经领取")
    @GetMapping("/{courseId}/valid")
    public Long isLessonValid(
            @ApiParam(value = "漫剧id" ,example = "1") @PathVariable("courseId") Long courseId){
        return lessonService.isLessonValid(courseId);
    }

    @ApiOperation("创建观看计划")
    @PostMapping("/plans")
    public void createWatchPlans(@Valid @RequestBody WatchPlanDTO planDTO){
        lessonService.createWatchPlan(planDTO.getCourseId(), planDTO.getFreq());
    }

    @ApiOperation("查询我的观看计划")
    @GetMapping("/plans")
    public WatchPlanPageVO queryMyPlans(PageQuery query){
        return lessonService.queryMyPlans(query);
    }
}

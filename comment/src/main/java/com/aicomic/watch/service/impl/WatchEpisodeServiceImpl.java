package com.aicomic.watch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.client.comic.EpisodeClient;
import com.aicomic.api.client.comic.ComicClient;
import com.aicomic.api.dto.IdAndNumDTO;
import com.aicomic.api.dto.comic.EpisodeSimpleInfoDTO;
import com.aicomic.api.dto.comic.ComicFullInfoDTO;
import com.aicomic.api.dto.comic.ComicSimpleInfoDTO;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.domain.query.PageQuery;
import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.utils.*;
import com.aicomic.watch.domain.po.WatchEpisode;
import com.aicomic.watch.domain.po.WatchRecord;
import com.aicomic.watch.domain.vo.WatchEpisodeVO;
import com.aicomic.watch.domain.vo.WatchPlanPageVO;
import com.aicomic.watch.domain.vo.WatchPlanVO;
import com.aicomic.watch.enums.EpisodeStatus;
import com.aicomic.watch.enums.PlanStatus;
import com.aicomic.watch.mapper.WatchEpisodeMapper;
import com.aicomic.watch.mapper.WatchRecordMapper;
import com.aicomic.watch.service.IWatchEpisodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 观众漫剧表 服务实现类
 * </p>
 *
 * @since 2022-12-02
 */
@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
@Slf4j
public class WatchEpisodeServiceImpl extends ServiceImpl<WatchEpisodeMapper, WatchEpisode> implements IWatchEpisodeService {

    private final ComicClient courseClient;
    private final EpisodeClient catalogueClient;
    private final WatchRecordMapper recordMapper;

    @Override
    @Transactional
    public void addUserLessons(Long userId, List<Long> courseIds) {
        // 1.查询漫剧有效期
        List<ComicSimpleInfoDTO> cInfoList = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(cInfoList)) {
            // 漫剧不存在，无法添加
            log.error("漫剧信息不存在，无法添加到追剧列表");
            return;
        }
        // 2.循环遍历，处理WatchEpisode数据
        List<WatchEpisode> list = new ArrayList<>(cInfoList.size());
        for (ComicSimpleInfoDTO cInfo : cInfoList) {
            WatchEpisode lesson = new WatchEpisode();
            // 2.1.获取过期时间
            Integer validDuration = cInfo.getValidDuration();
            if (validDuration != null && validDuration > 0) {
                LocalDateTime now = LocalDateTime.now();
                lesson.setCreateTime(now);
                lesson.setExpireTime(now.plusMonths(validDuration));
            }
            // 2.2.填充userId和courseId
            lesson.setUserId(userId);
            lesson.setCourseId(cInfo.getId());
            list.add(lesson);
        }
        // 3.批量新增
        saveBatch(list);
    }

    @Override
    public PageDTO<WatchEpisodeVO> queryMyLessons(PageQuery query) {
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.分页查询
        // select * from watch_episode where user_id = #{userId} order by latest_learn_time limit 0, 5
        Page<WatchEpisode> page = lambdaQuery()
                .eq(WatchEpisode::getUserId, userId) // where user_id = #{userId}
                .page(query.toMpPage("latest_learn_time", false));
        List<WatchEpisode> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        // 3.查询漫剧信息
        Map<Long, ComicSimpleInfoDTO> cMap = queryCourseSimpleInfoList(records);

        // 4.封装VO返回
        List<WatchEpisodeVO> list = new ArrayList<>(records.size());
        // 4.1.循环遍历，把WatchEpisode转为VO
        for (WatchEpisode r : records) {
            // 4.2.拷贝基础属性到vo
            WatchEpisodeVO vo = BeanUtils.copyBean(r, WatchEpisodeVO.class);
            // 4.3.获取漫剧信息，填充到vo
            ComicSimpleInfoDTO cInfo = cMap.get(r.getCourseId());
            vo.setCourseName(cInfo.getName());
            vo.setCourseCoverUrl(cInfo.getCoverUrl());
            vo.setSections(cInfo.getSectionNum());
            list.add(vo);
        }
        return PageDTO.of(page, list);
    }

    private Map<Long, ComicSimpleInfoDTO> queryCourseSimpleInfoList(List<WatchEpisode> records) {
        // 3.1.获取漫剧id
        Set<Long> cIds = records.stream().map(WatchEpisode::getCourseId).collect(Collectors.toSet());
        // 3.2.查询漫剧信息
        List<ComicSimpleInfoDTO> cInfoList = courseClient.getSimpleInfoList(cIds);
        if (CollUtils.isEmpty(cInfoList)) {
            // 漫剧不存在，无法添加
            throw new BadRequestException("漫剧信息不存在！");
        }
        // 3.3.把漫剧集合处理成Map，key是courseId，值是course本身
        Map<Long, ComicSimpleInfoDTO> cMap = cInfoList.stream()
                .collect(Collectors.toMap(ComicSimpleInfoDTO::getId, c -> c));
        return cMap;
    }

    @Override
    public WatchEpisodeVO queryMyCurrentLesson() {
        // 1.获取当前登录的用户
        Long userId = UserContext.getUser();
        // 2.查询正在观看的漫剧 select * from xx where user_id = #{userId} AND status = 1 order by latest_learn_time limit 1
        WatchEpisode lesson = lambdaQuery()
                .eq(WatchEpisode::getUserId, userId)
                .eq(WatchEpisode::getStatus, EpisodeStatus.WATCHING.getValue())
                .orderByDesc(WatchEpisode::getLatestLearnTime)
                .last("limit 1")
                .one();
        if (lesson == null) {
            return null;
        }
        // 3.拷贝PO基础属性到VO
        WatchEpisodeVO vo = BeanUtils.copyBean(lesson, WatchEpisodeVO.class);
        // 4.查询漫剧信息
        ComicFullInfoDTO cInfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
        if (cInfo == null) {
            throw new BadRequestException("漫剧不存在");
        }
        vo.setCourseName(cInfo.getName());
        vo.setCourseCoverUrl(cInfo.getCoverUrl());
        vo.setSections(cInfo.getSectionNum());
        // 5.统计追剧列表中的漫剧数量 select count(1) from xxx where user_id = #{userId}
        Integer courseAmount = lambdaQuery()
                .eq(WatchEpisode::getUserId, userId)
                .count();
        vo.setCourseAmount(courseAmount);
        // 6.查询片段信息
        List<EpisodeSimpleInfoDTO> cataInfos =
                catalogueClient.batchQueryCatalogue(CollUtils.singletonList(lesson.getLatestSectionId()));
        if (!CollUtils.isEmpty(cataInfos)) {
            EpisodeSimpleInfoDTO cataInfo = cataInfos.get(0);
            vo.setLatestSectionName(cataInfo.getName());
            vo.setLatestSectionIndex(cataInfo.getCIndex());
        }
        return vo;
    }

    @Override
    public WatchEpisodeVO queryLessonByCourseId(Long courseId) {
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.查询漫剧信息 select * from xx where user_id = #{userId} AND course_id = #{courseId}
        WatchEpisode lesson = getOne(buildUserIdAndCourseIdWrapper(userId, courseId));
        if (lesson == null) {
            return null;
        }
        // 3.处理VO
        return BeanUtils.copyBean(lesson, WatchEpisodeVO.class);
    }

    @Override
    public void deleteCourseFromLesson(Long userId, Long courseId) {
        // 1.获取当前登录用户
        if (userId == null) {
            userId = UserContext.getUser();
        }
        // 2.删除漫剧
        remove(buildUserIdAndCourseIdWrapper(userId, courseId));
    }

    @Override
    public Integer countWatchEpisodeByCourse(Long courseId) {
        // select count(1) from xx where course_id = #{cc} AND status in (0, 1, 2)
        return lambdaQuery()
                .eq(WatchEpisode::getCourseId, courseId)
                .in(WatchEpisode::getStatus,
                        EpisodeStatus.NOT_BEGIN.getValue(),
                        EpisodeStatus.WATCHING.getValue(),
                        EpisodeStatus.FINISHED.getValue())
                .count();
    }

    @Override
    public Long isLessonValid(Long courseId) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        if (userId == null) {
            return null;
        }
        // 2.查询漫剧
        WatchEpisode lesson = getOne(buildUserIdAndCourseIdWrapper(userId, courseId));
        if (lesson == null) {
            return null;
        }
        return lesson.getId();
    }

    @Override
    public WatchEpisode queryByUserAndCourseId(Long userId, Long courseId) {
        return getOne(buildUserIdAndCourseIdWrapper(userId, courseId));
    }

    @Override
    public void createWatchPlan(Long courseId, Integer freq) {
        // 1.获取当前登录的用户
        Long userId = UserContext.getUser();
        // 2.查询追剧列表中的指定漫剧有关的数据
        WatchEpisode lesson = queryByUserAndCourseId(userId, courseId);
        AssertUtils.isNotNull(lesson, "漫剧信息不存在！");
        // 3.修改数据
        WatchEpisode l = new WatchEpisode();
        l.setId(lesson.getId());
        l.setWeekFreq(freq);
        if(lesson.getPlanStatus() == PlanStatus.NO_PLAN) {
            l.setPlanStatus(PlanStatus.PLAN_RUNNING);
        }
        updateById(l);
    }

    @Override
    public WatchPlanPageVO queryMyPlans(PageQuery query) {
        WatchPlanPageVO result = new WatchPlanPageVO();
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.获取本周起始时间
        LocalDate now = LocalDate.now();
        LocalDateTime begin = DateUtils.getWeekBeginTime(now);
        LocalDateTime end = DateUtils.getWeekEndTime(now);
        // 3.查询总的统计数据
        // 3.1.本周总的已观看片段数量
        Integer weekFinished = recordMapper.selectCount(new LambdaQueryWrapper<WatchRecord>()
                .eq(WatchRecord::getUserId, userId)
                .eq(WatchRecord::getFinished, true)
                .gt(WatchRecord::getFinishTime, begin)
                .lt(WatchRecord::getFinishTime, end)
        );
        result.setWeekFinished(weekFinished);
        // 3.2.本周总的计划观看片段数量
        Integer weekTotalPlan = getBaseMapper().queryTotalPlan(userId);
        result.setWeekTotalPlan(weekTotalPlan);
        // TODO 3.3.本周观看积分

        // 4.查询分页数据
        // 4.1.分页查询追剧列表信息以及观看计划信息
        Page<WatchEpisode> p = lambdaQuery()
                .eq(WatchEpisode::getUserId, userId)
                .eq(WatchEpisode::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .in(WatchEpisode::getStatus, EpisodeStatus.NOT_BEGIN, EpisodeStatus.WATCHING)
                .page(query.toMpPage("latest_learn_time", false));
        List<WatchEpisode> records = p.getRecords();
        if (CollUtils.isEmpty(records)) {
            return result.emptyPage(p);
        }
        // 4.2.查询追剧列表对应的漫剧信息
        Map<Long, ComicSimpleInfoDTO> cMap = queryCourseSimpleInfoList(records);
        // 4.3.统计每一个漫剧本周已观看片段数量
        List<IdAndNumDTO> list = recordMapper.countLearnedSections(userId, begin, end);
        Map<Long, Integer> countMap = IdAndNumDTO.toMap(list);
        // 4.4.组装数据VO
        List<WatchPlanVO> voList = new ArrayList<>(records.size());
        for (WatchEpisode r : records) {
            // 4.4.1.拷贝基础属性到vo
            WatchPlanVO vo = BeanUtils.copyBean(r, WatchPlanVO.class);
            // 4.4.2.填充漫剧详细信息
            ComicSimpleInfoDTO cInfo = cMap.get(r.getCourseId());
            if (cInfo != null) {
                vo.setCourseName(cInfo.getName());
                vo.setSections(cInfo.getSectionNum());
            }
            // 4.4.3.每个漫剧的本周已观看片段数量
            vo.setWeekLearnedSections(countMap.getOrDefault(r.getId(), 0));
            voList.add(vo);
        }
        return result.pageInfo(p.getTotal(), p.getPages(), voList);
    }

    private LambdaQueryWrapper<WatchEpisode> buildUserIdAndCourseIdWrapper(Long userId, Long courseId) {
        LambdaQueryWrapper<WatchEpisode> queryWrapper = new QueryWrapper<WatchEpisode>()
                .lambda()
                .eq(WatchEpisode::getUserId, userId)
                .eq(WatchEpisode::getCourseId, courseId);
        return queryWrapper;
    }
}

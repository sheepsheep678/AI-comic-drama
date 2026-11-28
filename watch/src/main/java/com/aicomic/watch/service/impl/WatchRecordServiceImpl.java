package com.aicomic.watch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.client.comic.ComicClient;
import com.aicomic.api.dto.comic.ComicFullInfoDTO;
import com.aicomic.api.dto.watch.WatchEpisodeDTO;
import com.aicomic.api.dto.watch.WatchRecordDTO;
import com.aicomic.common.exceptions.BizIllegalException;
import com.aicomic.common.exceptions.DbException;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.common.utils.UserContext;
import com.aicomic.watch.domain.dto.WatchRecordFormDTO;
import com.aicomic.watch.domain.po.WatchEpisode;
import com.aicomic.watch.domain.po.WatchRecord;
import com.aicomic.watch.enums.EpisodeStatus;
import com.aicomic.watch.enums.ClipType;
import com.aicomic.watch.mapper.WatchRecordMapper;
import com.aicomic.watch.service.IWatchEpisodeService;
import com.aicomic.watch.service.IWatchRecordService;
import com.aicomic.watch.utils.WatchRecordDelayTaskHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 观看记录表 服务实现类
 * </p>
 *
 * @since 2022-12-10
 */
@Service
@RequiredArgsConstructor
public class WatchRecordServiceImpl extends ServiceImpl<WatchRecordMapper, WatchRecord> implements IWatchRecordService {

    private final IWatchEpisodeService lessonService;

    private final ComicClient courseClient;

    private final WatchRecordDelayTaskHandler taskHandler;

    @Override
    public WatchEpisodeDTO queryWatchRecordByCourse(Long courseId) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        // 2.查询追剧列表
        WatchEpisode lesson = lessonService.queryByUserAndCourseId(userId, courseId);
        // 3.查询观看记录
        // select * from xx where lesson_id = #{lessonId}
        List<WatchRecord> records = lambdaQuery().eq(WatchRecord::getLessonId, lesson.getId()).list();
        // 4.封装结果
        WatchEpisodeDTO dto = new WatchEpisodeDTO();
        dto.setId(lesson.getId());
        dto.setLatestSectionId(lesson.getLatestSectionId());
        dto.setRecords(BeanUtils.copyList(records, WatchRecordDTO.class));
        return dto;
    }

    @Override
    @Transactional
    public void addWatchRecord(WatchRecordFormDTO recordDTO) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        // 2.处理观看记录
        boolean finished = false;
        if (recordDTO.getSectionType() == ClipType.VIDEO) {
            // 2.1.处理视频
            finished = handleVideoRecord(userId, recordDTO);
        }else{
            // 2.2.处理答题
            finished = handleExamRecord(userId, recordDTO);
        }
        if(!finished){
            // 没有新学完的片段，无需更新追剧列表中的观看进度
            return;
        }
        // 3.处理追剧列表数据
        handleWatchEpisodesChanges(recordDTO);
    }

    private void handleWatchEpisodesChanges(WatchRecordFormDTO recordDTO) {
        // 1.查询追剧列表
        WatchEpisode lesson = lessonService.getById(recordDTO.getLessonId());
        if (lesson == null) {
            throw new BizIllegalException("漫剧不存在，无法更新数据！");
        }
        // 2.判断是否有新的完成片段
        boolean allLearned = false;

            // 3.如果有新完成的片段，则需要查询漫剧数据
            ComicFullInfoDTO cInfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
            if (cInfo == null) {
                throw new BizIllegalException("漫剧不存在，无法更新数据！");
            }
            // 4.比较漫剧是否全部学完：已观看片段 >= 漫剧总片段
            allLearned = lesson.getLearnedSections() + 1 >= cInfo.getSectionNum();

        // 5.更新追剧列表
        lessonService.lambdaUpdate()
                .set(lesson.getLearnedSections() == 0, WatchEpisode::getStatus, EpisodeStatus.WATCHING.getValue())
                .set(allLearned, WatchEpisode::getStatus, EpisodeStatus.FINISHED.getValue())
                .setSql("learned_sections = learned_sections + 1")
                .eq(WatchEpisode::getId, lesson.getId())
                .update();
    }

    private boolean handleVideoRecord(Long userId, WatchRecordFormDTO recordDTO) {
        // 1.查询旧的观看记录
        WatchRecord old = queryOldRecord(recordDTO.getLessonId(), recordDTO.getSectionId());
        // 2.判断是否存在
        if (old == null) {
            // 3.不存在，则新增
            // 3.1.转换PO
            WatchRecord record = BeanUtils.copyBean(recordDTO, WatchRecord.class);
            // 3.2.填充数据
            record.setUserId(userId);
            // 3.3.写入数据库
            boolean success = save(record);
            if (!success) {
                throw new DbException("新增观看记录失败！");
            }
            return false;
        }
        // 4.存在，则更新
        // 4.1.判断是否是第一次完成
        boolean finished = !old.getFinished() && recordDTO.getMoment() * 2 >= recordDTO.getDuration();
        if(!finished){
            WatchRecord record = new WatchRecord();
            record.setLessonId(recordDTO.getLessonId());
            record.setSectionId(recordDTO.getSectionId());
            record.setMoment(recordDTO.getMoment());
            record.setId(old.getId());
            record.setFinished(old.getFinished());
            taskHandler.addWatchRecordTask(record);
            return false;
        }
        // 4.2.更新数据
        boolean success = lambdaUpdate()
                .set(WatchRecord::getMoment, recordDTO.getMoment())
                .set(WatchRecord::getFinished, true)
                .set(WatchRecord::getFinishTime, recordDTO.getCommitTime())
                .eq(WatchRecord::getId, old.getId())
                .update();
        if(!success){
            throw new DbException("更新观看记录失败！");
        }
        // 4.3.清理缓存
        taskHandler.cleanRecordCache(recordDTO.getLessonId(), recordDTO.getSectionId());
        return true;
    }

    private WatchRecord queryOldRecord(Long lessonId, Long sectionId) {
        // 1.查询缓存
        WatchRecord record = taskHandler.readRecordCache(lessonId, sectionId);
        // 2.如果命中，直接返回
        if (record != null) {
            return record;
        }
        // 3.未命中，查询数据库
        record = lambdaQuery()
                .eq(WatchRecord::getLessonId, lessonId)
                .eq(WatchRecord::getSectionId, sectionId)
                .one();
        // 4.写入缓存
        taskHandler.writeRecordCache(record);
        return record;
    }

    private boolean handleExamRecord(Long userId, WatchRecordFormDTO recordDTO) {
        // 1.转换DTO为PO
        WatchRecord record = BeanUtils.copyBean(recordDTO, WatchRecord.class);
        // 2.填充数据
        record.setUserId(userId);
        record.setFinished(true);
        record.setFinishTime(recordDTO.getCommitTime());
        // 3.写入数据库
        boolean success = save(record);
        if (!success) {
            throw new DbException("新增答题记录失败！");
        }
        return true;
    }
}

package com.aicomic.comic.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.comic.constants.ComicConstants;
import com.aicomic.comic.domain.po.ComicEpisodeTagDraft;
import com.aicomic.comic.mapper.ComicEpisodeTagDraftMapper;
import com.aicomic.comic.service.IComicEpisodeTagDraftService;
import com.aicomic.comic.service.IComicEpisodeDraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 漫剧-题目关系表草稿 服务实现类
 * </p>
 *
 * @since 2022-09-21
 */
@Service
public class ComicEpisodeTagDraftServiceImpl extends ServiceImpl<ComicEpisodeTagDraftMapper, ComicEpisodeTagDraft> implements IComicEpisodeTagDraftService {

    @Autowired
    private IComicEpisodeDraftService comicEpisodeDraftService;

    @Override
    @Transactional
    public void deleteNotInCataIdList(Long courseId) {

        //1.获取所有漫剧的片段和练习
        List<Long> cataIdList = comicEpisodeDraftService.queryCataIdsOfCourse(courseId,
                Arrays.asList(
                        ComicConstants.EpisodeType.CLIP,
                        ComicConstants.EpisodeType.PRATICE
                ));
        //1.删除条件
        LambdaUpdateWrapper<ComicEpisodeTagDraft> updateWrapper =
                Wrappers.lambdaUpdate(ComicEpisodeTagDraft.class)
                        .eq(ComicEpisodeTagDraft::getCourseId, courseId)
                        .notIn(CollUtils.isNotEmpty(cataIdList),
                                ComicEpisodeTagDraft::getCataId, cataIdList);
        //2.删除题目
        baseMapper.delete(updateWrapper);
    }
}

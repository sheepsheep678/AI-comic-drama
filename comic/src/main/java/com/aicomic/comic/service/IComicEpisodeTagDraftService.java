package com.aicomic.comic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.comic.domain.po.ComicEpisodeTagDraft;

/**
 * <p>
 * 漫剧-题目关系表草稿 服务类
 * </p>
 *
 * @since 2022-09-21
 */
public interface IComicEpisodeTagDraftService extends IService<ComicEpisodeTagDraft> {
    /**
     * 删除不在的漫剧片段目录
     * @param courseId
     */
    void deleteNotInCataIdList(Long courseId);
}

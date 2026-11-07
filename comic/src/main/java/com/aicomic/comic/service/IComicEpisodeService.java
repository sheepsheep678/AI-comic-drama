package com.aicomic.comic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.api.dto.comic.EpisodeDTO;
import com.aicomic.api.dto.comic.MediaQuoteDTO;
import com.aicomic.api.dto.comic.ClipInfoDTO;
import com.aicomic.comic.domain.po.ComicEpisode;
import com.aicomic.comic.domain.vo.EpisodeSimpleInfoVO;
import com.aicomic.comic.domain.vo.EpisodeVO;

import java.util.List;

/**
 * <p>
 * 目录草稿 服务类
 * </p>
 *
 * @since 2022-07-19
 */
public interface IComicEpisodeService extends IService<ComicEpisode> {

    /**
     * 查询线上漫剧目录
     *
     * @param courseId 漫剧id
     * @return 漫剧目录
     */
    List<EpisodeDTO> queryComicEpisodes(Long courseId, Boolean withPractice);

    /**
     * 批量统计素材id引用次数
     *
     * @param mediaIds 素材id
     * @return 素材引用次数
     */
    List<MediaQuoteDTO> countMediaUserInfo(List<Long> mediaIds);

    /**
     * 获取简单的片段信息，
     *
     * @param sectionId 片段id
     * @return 漫剧id，素材id，是否支持免费试看，免费试看时长
     */
    ClipInfoDTO getSimpleSectionInfo(Long sectionId);

    /**
     * 根据漫剧id获取漫剧的目录列表
     *
     * @param courseId 漫剧id
     * @return 漫剧的目录列表
     */
    List<EpisodeSimpleInfoVO> getCatasIndexList(Long courseId);

    List<EpisodeSimpleInfoVO> getManyCataSimpleInfo(List<Long> ids);

    EpisodeSimpleInfoVO querySectionInfoById(Long id);

    List<EpisodeVO> queryComicEpisodesVO(Long courseId, Boolean withPractice);
}

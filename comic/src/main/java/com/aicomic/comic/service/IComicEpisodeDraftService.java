package com.aicomic.comic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.comic.domain.dto.EpisodeSaveDTO;
import com.aicomic.comic.domain.dto.EpisodeTagDTO;
import com.aicomic.comic.domain.dto.ComicMediaDTO;
import com.aicomic.comic.domain.po.ComicEpisodeDraft;
import com.aicomic.comic.domain.vo.EpisodeSimpleTagVO;
import com.aicomic.comic.domain.vo.EpisodeVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 目录草稿 服务类
 * </p>
 *
 * @since 2022-07-19
 */
public interface IComicEpisodeDraftService extends IService<ComicEpisodeDraft> {

    /**
     * 保存漫剧对应的目录结构
     *
     * @param courseId 漫剧id
     * @param episodeSaveDTOS 漫剧目录信息
     */
    void save(Long courseId, List<EpisodeSaveDTO> episodeSaveDTOS, Integer step);

    /**
     * 查询漫剧的目录
     *
     * @param courseId 漫剧id必填
     * @param see      是否用于查看数据
     * @return
     */
    List<EpisodeVO> queryComicEpisodes(Long courseId, Boolean see, Boolean withPractice);

    /**
     * 保存素材信息
     */
    void saveMediaInfo(Long courseId, List<ComicMediaDTO> courseMediaDTOS);

    void saveSuject(Long courseId, List<EpisodeTagDTO> cataSubjectDTOS);

    /**
     * 根据漫剧id，获取题目，用于草稿编辑
     *
     * @param courseId
     * @return
     */
    List<EpisodeSimpleTagVO> getSuject(Long courseId);

    /**
     * 校验漫剧对应的目录数据完整，包含  视频，题目，
     *
     * @param courseId
     */
    void checkCataInfoImplated(Long courseId);

    /**
     * 复制试题到架上
     *
     * @param courseId
     * @param isFirstShelf
     */
    void copySubjectToShelf(Long courseId, Boolean isFirstShelf);

    /**
     * copy目录到架上
     *
     * @param courseId
     * @param isFirstShelf
     */
    void copyToShelf(Long courseId, Boolean isFirstShelf);

    /**
     * 计算当前漫剧中每个章素材时长
     *
     * @param courseId
     * @return
     */
    Map<Long, Integer> calculateMediaDuration(Long courseId);

    /**
     * 漫剧总节和练习数，不包含章
     *
     * @param courseId
     * @return
     */
    Integer totalSectionNums(Long courseId);

    /**
     * 根据类型查询漫剧片段/章/测试id列表
     *
     * @param courseId
     * @param types
     * @return
     */
    List<Long> queryCataIdsOfCourse(Long courseId, List<Integer> types);

}

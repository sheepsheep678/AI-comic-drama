package com.aicomic.comic.service;

import com.aicomic.comic.domain.dto.ComicCreatorSaveDTO;
import com.aicomic.comic.domain.po.ComicCreatorDraft;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.comic.domain.vo.ComicCreatorVO;

import java.util.List;

/**
 * <p>
 * 漫剧创作者关系表草稿 服务类
 * </p>
 *
 * @since 2022-07-20
 */
public interface IComicCreatorDraftService extends IService<ComicCreatorDraft> {

    /**
     * 保存漫剧指定的创作者
     * @param comicCreatorSaveDTO 创作者数据
     */
    void save(ComicCreatorSaveDTO comicCreatorSaveDTO);

    /**
     * 查询指定漫剧对应的创作者
     *
     * @param courseId 漫剧id
     * @param see 是否用于查看
     * @return 创作者数据
     */
    List<ComicCreatorVO> queryTeacherOfCourse(Long courseId,Boolean see);

    /**
     * 漫剧创作者上架
     * @param courseId 漫剧id
     */
    void copyToShelf(Long courseId, Boolean isFirstShelf);

}

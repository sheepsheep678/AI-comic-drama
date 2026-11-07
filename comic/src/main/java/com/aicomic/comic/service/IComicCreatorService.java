package com.aicomic.comic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.comic.domain.po.ComicCreator;
import com.aicomic.comic.domain.vo.ComicCreatorVO;

import java.util.List;

/**
 * <p>
 * 漫剧创作者关系表草稿 服务类
 * </p>
 *
 * @since 2022-07-20
 */
public interface IComicCreatorService extends IService<ComicCreator> {

    /**
     * 查询创作者漫剧信息
     * @param couserId 漫剧id
     * @return 创作者信息
     */
    List<ComicCreatorVO> queryTeachers(Long couserId);

    void deleteByCourseId(Long courserId);

    /**
     * 根据漫剧id获取创作者id列表，并且排序
     * @param courseId 漫剧id
     * @return 创作者信息
     */
    List<Long> getTeacherIdOfCourse(Long courseId);


}

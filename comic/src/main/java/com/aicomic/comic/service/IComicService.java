package com.aicomic.comic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.api.dto.comic.*;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.comic.domain.dto.ComicPageQuery;
import com.aicomic.comic.domain.dto.ComicSimpleInfoListDTO;
import com.aicomic.comic.domain.po.Comic;
import com.aicomic.comic.domain.vo.ComicAndClipVO;
import com.aicomic.comic.domain.vo.ComicPageVO;
import com.aicomic.comic.domain.vo.NameExistVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 草稿漫剧 服务类
 * </p>
 *
 * @since 2022-07-20
 */
public interface IComicService extends IService<Comic> {

    /**
     * 修改漫剧状态
     *
     * @param id 漫剧id
     * @param status 漫剧状态
     */
    void updateStatus(Long id, Integer status);

    ComicDTO getCourseDTOById(Long id);


    void delete(Long id);

    /**
     * 根据条件查询漫剧简单信息
     *
     * @param courseSimpleInfoListDTO 漫剧三级分类列表
     * @return 漫剧信息列表
     */
    List<ComicSimpleInfoDTO> getSimpleInfoList(ComicSimpleInfoListDTO courseSimpleInfoListDTO);

    /**
     * 查询创作者出题数目和漫剧数目
     * @param creatorIds 创作者id
     * @return 创作者的出题数量
     */
    List<SubNumAndComicNumDTO> countSubjectNumAndCourseNumOfTeacher(List<Long> creatorIds);

    /**
     * 漫剧完结
     */
    int courseFinished();

    /**
     * 统计每个分类id所拥有的漫剧数量
     * @return 分类对应的漫剧数量
     */
    Map<Long, Integer> countCourseNumOfCategory();

    /**
     * 查询有已上架漫剧分类的分类id
     * @return
     */
    List<Long> getCategoryIdListWithCourse();

    /**
     * 统计某个漫剧分类的漫剧数量
     *
     * @param categoryId 漫剧分类id
     * @return 漫剧数量
     */
    Integer countCourseNumOfCategory(Long categoryId);

    /**
     * 查询漫剧的详细信息
     * @param id 漫剧id
     * @param withCatalogue 是否查询目录数据
     * @param withTeachers 是否查询创作者数据
     * @return 漫剧详细信息
     */
    ComicFullInfoDTO getInfoById(Long id, boolean withCatalogue, boolean withTeachers);

    /**
     * 分页查询漫剧信息
     * @param coursePageQuery 分页参数
     * @return 漫剧分页数据
     */
    PageDTO<ComicPageVO> queryForPage(ComicPageQuery coursePageQuery);
    /**
     * 根据漫剧分类id查询漫剧列表
     * @param categoryId 漫剧分类id
     * @param level 漫剧分类级别
     * @return
     */
    List<Comic> queryByCategoryIdAndLevel(Long categoryId, Integer level);

    /**
     * 校验名称是否存在，或者被其他漫剧占用
     * @param name 漫剧名称
     * @param id 当前漫剧名称
     */
    NameExistVO checkName(String name, Long id);

    /**
     * 查询漫剧id列表中
     * @param idList
     * @return
     */
    List<Long> queryExists(List<Long> idList,List<Integer> statusList);

    /**
     * 根据漫剧name模糊查询漫剧id列表
     * @param name
     * @return
     */
    List<Long> queryCourseIdByName(String name);

    ComicAndClipVO queryCourseAndCatalogById(Long courseId);
}

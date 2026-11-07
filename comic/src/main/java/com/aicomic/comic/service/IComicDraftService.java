package com.aicomic.comic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.api.dto.comic.ComicDTO;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.comic.domain.dto.ComicBaseInfoSaveDTO;
import com.aicomic.comic.domain.dto.ComicPageQuery;
import com.aicomic.comic.domain.po.ComicDraft;
import com.aicomic.comic.domain.vo.ComicBaseInfoVO;
import com.aicomic.comic.domain.vo.ComicPageVO;
import com.aicomic.comic.domain.vo.ComicSaveVO;
import com.aicomic.comic.domain.vo.NameExistVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 草稿漫剧 服务类
 * </p>
 *
 * @since 2022-07-18
 */
public interface IComicDraftService extends IService<ComicDraft> {

    /**
     * 保存草稿
     *
     * @param courseBaseInfoSaveDTO 漫剧基础信息
     */
    ComicSaveVO save(ComicBaseInfoSaveDTO courseBaseInfoSaveDTO);

    /**
     * 如果用于编辑，需要先去草稿中拿出已经编辑的内容，如果不是用于编辑直接获取正式数据
     *
     * @param id 漫剧id
     * @param see 是否用于查看页面查看数据使用，不是的话就是编辑页面使用
     * @return 漫剧基本信息
     */
    ComicBaseInfoVO getCourseBaseInfo(Long id, Boolean see);

    /**
     * 修改漫剧草稿进行到哪一步了，步骤只能一步步升，不能跳填，不能往回填
     * @param id 漫剧id
     * @param step 完成的步数
     */
    void updateStep(Long id, Integer step);

    /**
     * 漫剧上架
     *
     * @param id 漫剧上架
     */
    void upShelf(Long id);

    void checkBeforeUpShelf(Long id);

    /**
     * 漫剧下架
     *
     * @param id 漫剧id
     */
    void downShelf(Long id);

    /**
     * 获取漫剧的搜索信息
     * @param id 漫剧id
     * @return 漫剧数据
     */
    ComicDTO getCourseDTOById(Long id);

    /**
     * 删除漫剧的草稿
     *
     * @param id 漫剧id
     */
    void delete(Long id);

    /**
     * 分页查询更新时间
     * @param coursePageQuery 漫剧分页参数
     * @return 漫剧分页数据
     */
    PageDTO<ComicPageVO> queryForPage(ComicPageQuery coursePageQuery);

    /**
     * 校验名称是否存在，或者被其他漫剧占用
     * @param name 漫剧名称
     * @param id 当前漫剧名称
     */
    NameExistVO checkName(String name, Long id);

    /**
     * 查询漫剧id列表中存在的id
     * @param idList
     * @return
     */
    List<Long> queryExists(List<Long> idList);

    /**
     * 统计草稿中的漫剧分类拥有的漫剧数量
     * @return
     */
    Map<Long, Integer> countCourseNumOfCategory();
}

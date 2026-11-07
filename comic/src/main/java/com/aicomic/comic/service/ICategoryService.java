package com.aicomic.comic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.comic.domain.dto.CategoryAddDTO;
import com.aicomic.comic.domain.dto.CategoryDisableOrEnableDTO;
import com.aicomic.comic.domain.dto.CategoryListDTO;
import com.aicomic.comic.domain.dto.CategoryUpdateDTO;
import com.aicomic.comic.domain.po.Category;
import com.aicomic.comic.domain.po.Comic;
import com.aicomic.comic.domain.vo.CategoryInfoVO;
import com.aicomic.comic.domain.vo.CategoryVO;
import com.aicomic.comic.domain.vo.SimpleCategoryVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 漫剧分类 服务类
 * </p>
 *
 * @since 2022-07-14
 */
public interface ICategoryService extends IService<Category> {

    /**
     * 分页查询漫剧信息
     *
     * @param categoryPageDTO 分页参数
     * @return 漫剧分页信息
     */
    List<CategoryVO> list(CategoryListDTO categoryPageDTO);

    /**
     * 新增漫剧分页
     *
     * @param categoryAddDTO 分类信息
     */
    void add(CategoryAddDTO categoryAddDTO);

    /**
     * 获取漫剧分类信息
     * @param id 漫剧id
     * @return 漫剧分类信息
     */
    CategoryInfoVO get(Long id);

    /**
     * 删除漫剧分类
     * @param id 分类id
     */
    void delete(Long id);

    /**
     * 漫剧分类启用或禁用
     */
    void disableOrEnable(CategoryDisableOrEnableDTO categoryDisableOrEnableDTO);

    /**
     * 更新漫剧分类信息
     */
    void update(CategoryUpdateDTO categoryUpdateDTO);

    /**
     * 获取所有分类的数据及结构
     */
    List<SimpleCategoryVO> all(Boolean admin);

    /**
     * 获取漫剧分类id和名称
     * @return 漫剧分类id和名称
     */
    Map<Long, String> getCateIdAndName();

    List<CategoryVO> allOfOneLevel();

    /**
     * 根据漫剧分类id查询分类列表
     * @param ids  漫剧分类id
     * @return 分类列表
     */
    List<Category> queryByIds(List<Long> ids);

    /**
     * 根据三级漫剧分类查询漫剧分类信息
     * @param thirdCateIdList 三级漫剧分类
     * @return 漫剧分类信息
     */
    Map<Long, String> queryByThirdCateIds(@RequestParam("thirdCateIdList") List<Long> thirdCateIdList);

    /**
     * 获取漫剧分类信息
     *
     * @param course
     * @return
     */
    List<String> queryCourseCategorys(Comic course);

    /**
     * 校验漫剧分类是否符合要求,并按顺序返回一二三级漫剧分类id列表
     *
     * @param thirdCateId 三级漫剧分类
     * @return 一二三级漫剧分类id列表
     */
    List<Long> checkCategory(Long thirdCateId);
}

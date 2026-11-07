package com.aicomic.search.controller;


import com.aicomic.search.domain.vo.ComicVO;
import com.aicomic.search.service.IInterestsService;
import com.aicomic.search.service.ISearchService;
import com.aicomic.api.dto.comic.CategoryBasicDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户兴趣表，保存感兴趣的二级分类id 前端控制器
 * </p>
 *
 * @since 2022-07-21
 */
@RestController
@RequestMapping("/interests")
@Api(tags = "漫剧推荐相关接口")
@RequiredArgsConstructor
public class InterestsController {

    private final IInterestsService interestsService;
    private final ISearchService searchService;

    @ApiOperation("新增兴趣爱好")
    @PostMapping
    public void saveMyInterests(@RequestParam("interestedIds") List<Long>interestedIds){
        interestsService.saveInterests(interestedIds);
    }

    @ApiOperation("查询我的兴趣爱好")
    @GetMapping
    public List<CategoryBasicDTO> queryMyInterests(){
        return interestsService.queryMyInterests();
    }


    @ApiOperation("根据二级分类id查询漫剧TOP10")
    @GetMapping("/{id}/comics")
    public List<ComicVO> queryCourseByCateId(@PathVariable("id") Long cateLv2Id){
        return searchService.queryCourseByCateId(cateLv2Id);
    }
}
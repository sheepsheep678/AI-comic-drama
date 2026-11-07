package com.aicomic.search.controller;

import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.search.domain.query.ComicPageQuery;
import com.aicomic.search.domain.vo.ComicVO;
import com.aicomic.search.service.IComicService;
import com.aicomic.search.service.ISearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("comics")
@Api(tags = "漫剧搜索接口")
@RequiredArgsConstructor
public class ComicController {

    private final ISearchService searchService;
    private final IComicService courseService;

    @ApiOperation("用户端漫剧搜索接口")
    @GetMapping("/portal")
    public PageDTO<ComicVO> queryCoursesForPortal(ComicPageQuery query){
        return searchService.queryCoursesForPortal(query);
    }

    @ApiIgnore
    @GetMapping("/name")
    public List<Long> queryCoursesIdByName(@RequestParam("keyword") String keyword){
        return searchService.queryCoursesIdByName(keyword);
    }

    @ApiOperation("处理指定漫剧上架失败的问题")
    @PostMapping("/up")
    public void handleCoursesUp(
            @ApiParam("漫剧id集合") @RequestParam("courseIds") List<Long> courseIds) {
        for (Long courseId : courseIds) {
            courseService.handleCourseUp(courseId);
        }
    }

    @ApiOperation("处理指定漫剧下架失败的问题")
    @PostMapping("/down")
    public void handleCoursesDown(
            @ApiParam("漫剧id集合") @RequestParam("courseIds") List<Long> courseIds) {
        for (Long courseId : courseIds) {
            courseService.handleCourseDeletes(courseIds);
        }
    }
}

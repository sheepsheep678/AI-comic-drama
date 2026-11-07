package com.aicomic.comic.controller;

import com.aicomic.api.dto.comic.*;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.comic.service.ICategoryService;
import com.aicomic.comic.service.IComicEpisodeService;
import com.aicomic.comic.service.IComicService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 内部服务接口调用
 *
 * @ClassName ComicInfoController
 * @Author 
 * @Date 2022/7/18 15:19
 * @Version
 **/
@RestController
@RequestMapping("comic")
@Api(tags = "漫剧相关接口，内部调用")
public class ComicInfoController {

    @Autowired
    private IComicEpisodeService comicEpisodeService;

    @Autowired
    private IComicService courseService;

    @Autowired
    private ICategoryService categoryService;

    @GetMapping("infoByTeacherIds")
    @ApiOperation("通过创作者id获取创作者负责的漫剧和出的题目数量")
    public List<SubNumAndComicNumDTO> infoByTeacherIds(@RequestParam("creatorIds") List<Long> creatorIds) {

        if (CollUtils.isEmpty(creatorIds)) {
            return new ArrayList<>();
        }
        return courseService.countSubjectNumAndCourseNumOfTeacher(creatorIds);
    }

    /**
     * 根据片段id获取片段对应的mediaId和漫剧id
     *
     * @param sectionId 片段id
     * @return 片段对应的mediaId和漫剧id
     */
    @GetMapping("/section/{id}")
    @ApiImplicitParam(name = "id", value = "片段id，不支持章id或者练习id查询")
    public ClipInfoDTO sectionInfo(@PathVariable("id") Long sectionId) {
        return comicEpisodeService.getSimpleSectionInfo(sectionId);
    }

    /**
     * 根据素材Id列表查询素材被引用的次数
     *
     * @param mediaIds 素材id列表
     * @return 素材id和素材被引用的次数的列表
     */
    @GetMapping("/media/useInfo")
    public List<MediaQuoteDTO> mediaUserInfo(@RequestParam("mediaIds") List<Long> mediaIds) {
        return comicEpisodeService.countMediaUserInfo(mediaIds);
    }

    @GetMapping("/{id}/searchInfo")
    @ApiOperation("漫剧上架时，需要查询漫剧信息，加入索引库")
    public ComicDTO getSearchInfo(@ApiParam("漫剧id") @PathVariable("id") Long id) {
        return courseService.getCourseDTOById(id);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取漫剧信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "获取漫剧信息"),
            @ApiImplicitParam(name = "withCatalogue", value = "是否要查询目录信息"),
            @ApiImplicitParam(name = "withTeachers", value = "是否查询漫剧创作者信息")
    })
    public ComicFullInfoDTO getById(
            @PathVariable("id") Long id,
            @RequestParam(value = "withCatalogue", required = false) boolean withCatalogue,
            @RequestParam(value = "withTeachers", required = false) boolean withTeachers) {
        return courseService.getInfoById(id, withCatalogue, withTeachers);
    }


    @GetMapping("/getCateNameMap")
    @ApiIgnore
    public Map<Long, String> queryByThirdCateIds(@RequestParam("thirdCateIdList") List<Long> thirdCateIdList) {
        return categoryService.queryByThirdCateIds(thirdCateIdList);
    }

    @GetMapping("/name")
    public List<Long> queryCoursesIdByName(@RequestParam("name") String name){
        return courseService.queryCourseIdByName(name);
    }
}

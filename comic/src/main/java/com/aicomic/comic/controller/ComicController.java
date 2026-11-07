package com.aicomic.comic.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.aicomic.api.dto.comic.ComicSimpleInfoDTO;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.validate.annotations.ParamChecker;
import com.aicomic.comic.constants.ComicStatus;
import com.aicomic.comic.domain.dto.*;
import com.aicomic.comic.domain.vo.*;
import com.aicomic.comic.service.*;
import com.aicomic.comic.utils.ComicSaveBaseGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 漫剧controller
 *
 * @ClassName ComicController
 * @Author 
 * @Date 2022/7/10 15:34
 * @Version
 **/
@Api(tags = "漫剧相关接口")
@RestController
@RequestMapping("comics")
@Slf4j
@Validated
public class ComicController {

    @Autowired
    private IComicDraftService comicDraftService;

    @Autowired
    private IComicEpisodeDraftService comicEpisodeDraftService;

    @Autowired
    private IComicCreatorDraftService comicCreatorDraftService;

    @Autowired
    private IComicService courseService;

    @Autowired
    private IComicEpisodeService comicEpisodeService;

    //todo 二期做
//    @GetMapping("statistics")
//    @ApiOperation("漫剧数据统计")
    public ComicStatisticsVO statistics() {
        return null;
    }

    @GetMapping("baseInfo/{id}")
    @ApiOperation("获取漫剧基础信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "漫剧id"),
            @ApiImplicitParam(name = "see", value = "是否是用于查看页面查看数据，默认是查看,如果不是界面查看数据就是编辑页面使用")})
    public ComicBaseInfoVO baseInfo(@PathVariable("id") Long id,
                                     @RequestParam(value = "see", required = false, defaultValue = "1") Boolean see) {
        return comicDraftService.getCourseBaseInfo(id, see);
    }

    @PostMapping("baseInfo/save")
    @ApiOperation("保存漫剧基本信息")
    @ParamChecker
    //校验非业务限制的字段
    public ComicSaveVO save(@RequestBody @Validated(ComicSaveBaseGroup.class) ComicBaseInfoSaveDTO courseBaseInfoSaveDTO) {
        return comicDraftService.save(courseBaseInfoSaveDTO);
    }

    @GetMapping("catas/{id}")
    @ApiOperation("获取漫剧的章节")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "漫剧id"),
            @ApiImplicitParam(name = "see", value = "是否是用于查看页面查看数据，默认是查看,如果不是界面查看数据就是编辑页面使用")
    })
    public List<EpisodeVO> catas(@PathVariable(value = "id", required = false) Long id,
                              @RequestParam(value = "see", required = false, defaultValue = "1") Boolean see,
                              @RequestParam(value = "withPractice", required = false, defaultValue = "1") Boolean withPractice) {
        return comicEpisodeDraftService.queryComicEpisodes(id, see, withPractice);
    }

    @PostMapping("catas/save/{id}/{step}")
    @ApiOperation("保存章节")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "漫剧id"),
            @ApiImplicitParam(name = "step", value = "步骤")
    })
    @ParamChecker
    public void catasSave(@RequestBody @Validated List<EpisodeSaveDTO> episodeSaveDTOS,
                          @PathVariable("id") Long id, @PathVariable(value = "step",required = false) Integer step) {
        comicEpisodeDraftService.save(id, episodeSaveDTOS, step);
    }

    @PostMapping("media/save/{id}")
    @ApiOperation("漫剧视频")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "漫剧id")
    })
    public void mediaSave(@PathVariable("id") Long id, @RequestBody @Valid List<ComicMediaDTO> courseMediaDTOS) {
        comicEpisodeDraftService.saveMediaInfo(id, courseMediaDTOS);
    }

    @PostMapping("tags/save/{id}")
    @ApiOperation("保存片段或练习中的题目")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "漫剧id")
    })
    public void saveSuject(@PathVariable("id") Long id, @RequestBody @Validated List<EpisodeTagDTO> cataSubjectDTO) {
        comicEpisodeDraftService.saveSuject(id, cataSubjectDTO);
    }

    @GetMapping("tags/get/{id}")
    @ApiOperation("获取片段或练习中的题目（用于编辑）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "漫剧id")
    })
    public List<EpisodeSimpleTagVO> getSuject(@PathVariable("id") Long id) {
        return comicEpisodeDraftService.getSuject(id);
    }

    @GetMapping("creators/{id}")
    @ApiOperation("查询漫剧相关的创作者信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "漫剧id"),
            @ApiImplicitParam(name = "see", value = "是否是用于查看页面查看数据，默认是查看,如果不是界面查看数据就是编辑页面使用")
    })
    public List<ComicCreatorVO> creator(@PathVariable("id") Long id,
                                         @RequestParam(value = "see", required = false, defaultValue = "1") Boolean see) {
        return comicCreatorDraftService.queryTeacherOfCourse(id, see);
    }

    @PostMapping("creators/save")
    @ApiOperation("保存创作者信息")
    public void creatorsSave(@RequestBody @Validated ComicCreatorSaveDTO comicCreatorSaveDTO) {
        comicCreatorDraftService.save(comicCreatorSaveDTO);
    }


    @PostMapping("upShelf")
    @ApiOperation("漫剧上架")
    public void upShelf(@RequestBody @Validated ComicIdDTO courseIdDTO) {
        comicDraftService.upShelf(courseIdDTO.getId());
    }

    @GetMapping("checkBeforeUpShelf/{id}")
    @ApiOperation("漫剧上架前校验")
    public void checkBeforeUpShelf(@PathVariable("id") Long id){
        comicDraftService.checkBeforeUpShelf(id);
    }

    @PostMapping("downShelf")
    @ApiOperation("漫剧下架")
    public void downShelf(@RequestBody @Validated ComicIdDTO courseIdDTO) {
        comicDraftService.downShelf(courseIdDTO.getId());
    }

    /**
     * 先去删除加上数据删除后，再去删除草稿
     *
     * @param id
     */
    @DeleteMapping("delete/{id}")
    @ApiOperation("漫剧删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id")
    })
    public void deleteById(@PathVariable("id") Long id) {
        courseService.delete(id);
    }

    @ApiOperation("根根条件列表获取漫剧信息")
    @GetMapping("/simpleInfo/list")
    public List<ComicSimpleInfoDTO> getSimpleInfoList(ComicSimpleInfoListDTO courseSimpleInfoListDTO) {
        return courseService.getSimpleInfoList(courseSimpleInfoListDTO);
    }

    @ApiOperation("根据漫剧id，查询所有章节的序号")
    @GetMapping("/catas/index/list/{id}")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "id", value = "漫剧id")
    )
    public List<EpisodeSimpleInfoVO> catasIndexList(@PathVariable("id") Long id) {
        return comicEpisodeService.getCatasIndexList(id);
    }

    @ApiOperation("生成练习id")
    @GetMapping("generator")
    public ComicEpisodeIdVO generator() {
        return new ComicEpisodeIdVO(IdWorker.getId());
    }

    @ApiOperation("管理端漫剧搜索接口")
    @GetMapping("/page")
    public PageDTO<ComicPageVO> queryForPage(ComicPageQuery coursePageQuery) {
        if(ComicStatus.NO_UP_SHELF.equals(coursePageQuery.getStatus()) ||
        ComicStatus.DOWN_SHELF.equals(coursePageQuery.getStatus())){
            //待上架已下架查询草稿
            return comicDraftService.queryForPage(coursePageQuery);
        }else {
            //已上架已完结查询正式数据
            return courseService.queryForPage(coursePageQuery);
        }
    }

    @ApiOperation("校验漫剧名称是否已经存在")
    @GetMapping("/checkName")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id"),
            @ApiImplicitParam(name = "name", value = "漫剧名称")
    })
    public NameExistVO checkNameExist(@RequestParam(value = "id",required = false) Long id,
                                      @RequestParam(value = "name") String name){
        return courseService.checkName(name, id);
    }

    @ApiOperation("查询漫剧基本信息、目录、观看进度")
    @GetMapping("/{id}/catalogs")
    public ComicAndClipVO queryCourseAndCatalogById(@PathVariable("id") Long courseId){
        return courseService.queryCourseAndCatalogById(courseId);
    }
}

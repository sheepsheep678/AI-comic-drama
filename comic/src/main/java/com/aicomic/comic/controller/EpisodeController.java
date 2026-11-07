package com.aicomic.comic.controller;

import com.aicomic.comic.domain.vo.EpisodeSimpleInfoVO;
import com.aicomic.comic.service.IComicEpisodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 目录漫剧相关接口
 *
 * @ClassName EpisodeController
 * @Author 
 * @Date 2022/7/27 13:59
 * @Version
 **/
@Api(tags = "章节目录相关接口")
@RestController
@RequestMapping("episodes")
public class EpisodeController {

    @Autowired
    private IComicEpisodeService comicEpisodeService;

    @GetMapping("batchQuery")
    @ApiOperation("根据章节目录批量查询基础信息")
    public List<EpisodeSimpleInfoVO> batchQuery(@RequestParam("ids") List<Long> ids) {
        return comicEpisodeService.getManyCataSimpleInfo(ids);
    }

    @GetMapping("querySectionInfoById/{id}")
    @ApiOperation("获取片段信息")
    public EpisodeSimpleInfoVO querySectionInfoById(@PathVariable("id") Long id) {
        return comicEpisodeService.querySectionInfoById(id);
    }
}

package com.aicomic.watch.controller;


import com.aicomic.api.dto.watch.WatchEpisodeDTO;
import com.aicomic.watch.domain.dto.WatchRecordFormDTO;
import com.aicomic.watch.service.IWatchRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 观看记录表 前端控制器
 * </p>
 *
 * @since 2022-12-10
 */
@RestController
@RequestMapping("/watch-records")
@Api(tags = "观看记录的相关接口")
@RequiredArgsConstructor
public class WatchRecordController {

    private final IWatchRecordService recordService;

    @ApiOperation("查询指定漫剧的观看记录")
    @GetMapping("/comic/{courseId}")
    public WatchEpisodeDTO queryWatchRecordByCourse(
            @ApiParam(value = "漫剧id", example = "2") @PathVariable("courseId") Long courseId){
        return recordService.queryWatchRecordByCourse(courseId);
    }

    @ApiOperation("提交观看记录")
    @PostMapping
    public void addWatchRecord(@RequestBody WatchRecordFormDTO formDTO){
        recordService.addWatchRecord(formDTO);
    }
}

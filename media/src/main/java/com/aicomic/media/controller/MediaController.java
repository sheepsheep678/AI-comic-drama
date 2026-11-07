package com.aicomic.media.controller;


import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.media.domain.dto.MediaDTO;
import com.aicomic.media.domain.dto.MediaUploadResultDTO;
import com.aicomic.media.domain.query.MediaQuery;
import com.aicomic.media.domain.vo.MediaVO;
import com.aicomic.media.domain.vo.VideoPlayVO;
import com.aicomic.media.service.IMediaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 素材表，主要是视频文件 前端控制器
 * </p>
 *
 * @since 2022-06-30
 */
@RestController
@RequestMapping("/medias")
@Api(tags = "素材管理相关接口")
@RequiredArgsConstructor
public class MediaController {

    private final IMediaService mediaService;

    @ApiOperation("分页搜索已上传素材信息")
    @GetMapping
    public PageDTO<MediaVO> queryMediaPage(MediaQuery query){
        return mediaService.queryMediaPage(query);
    }

    @ApiOperation("上传视频后保存素材信息")
    @PostMapping
    public MediaDTO saveMedia(@RequestBody MediaUploadResultDTO result) {
        return mediaService.save(result);
    }

    @ApiOperation("获取上传视频的授权签名")
    @GetMapping("/signature/upload")
    public String getUploadSignature(){
        return mediaService.getUploadSignature();
    }

    @ApiOperation("获取播放视频的授权签名")
    @GetMapping("/signature/play")
    public VideoPlayVO getPlaySignature(
            @ApiParam(value = "片段id", example = "1", required = true) @RequestParam("sectionId") Long sectionId){
        return mediaService.getPlaySignatureBySectionId(sectionId);
    }

    @ApiOperation("管理端获取预览视频的授权签名")
    @GetMapping("/signature/preview")
    public VideoPlayVO getPreviewSignature(
            @ApiParam(value = "素材id", example = "1", required = true) @RequestParam("mediaId") Long mediaId){
        return mediaService.getPlaySignatureByMediaId(mediaId);
    }

    @ApiOperation("删除素材视频")
    @DeleteMapping("{mediaId}")
    public void deleteMedia(
            @ApiParam(value = "素材id", example = "1", required = true) @PathVariable("mediaId") Long mediaId){
        mediaService.removeById(mediaId);
    }

    @ApiOperation("批量删除素材视频")
    @DeleteMapping
    public void deleteMedias(
            @ApiParam(value = "素材id集合，例如1,2,3", required = true) @RequestParam("ids") List<Long> mediaIds){
        mediaService.removeByIds(mediaIds);
    }
}

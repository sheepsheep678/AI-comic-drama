package com.aicomic.api.client.comic;

import com.aicomic.api.dto.comic.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "comic", value = "comic-service")
public interface ComicClient {

    /**
     * 根据创作者id列表获取创作者出题数据和讲课数据
     * @param creatorIds 创作者id列表
     * @return 创作者id和创作者对应的出题数和教课数
     */
    @GetMapping("/comic/infoByTeacherIds")
    List<SubNumAndComicNumDTO> infoByTeacherIds(@RequestParam("creatorIds") Iterable<Long> creatorIds);

    /**
     * 根据片段id获取片段对应的mediaId和漫剧id
     *
     * @param sectionId 片段id
     * @return 片段对应的mediaId和漫剧id
     */
    @GetMapping("/comic/section/{id}")
    ClipInfoDTO sectionInfo(@PathVariable("id") Long sectionId);

    /**
     * 根据素材Id列表查询素材被引用的次数
     *
     * @param mediaIds 素材id列表
     * @return 素材id和素材被引用的次数的列表
     */
    @GetMapping("/comic/media/useInfo")
    List<MediaQuoteDTO> mediaUserInfo(@RequestParam("mediaIds") Iterable<Long> mediaIds);

    /**
     * 根据漫剧id查询索引库需要的数据
     *
     * @param id 漫剧id
     * @return 索引库需要的数据
     */
    @GetMapping("/comic/{id}/searchInfo")
    ComicSearchDTO getSearchInfo(@PathVariable("id") Long id);

    /**
     * 根据漫剧id集合查询漫剧简单信息
     * @param ids id集合
     * @return 漫剧简单信息的列表
     */
    @GetMapping("/comics/simpleInfo/list")
    List<ComicSimpleInfoDTO> getSimpleInfoList(@RequestParam("ids") Iterable<Long> ids);

    /**
     * 根据漫剧id，获取漫剧、目录、创作者信息
     * @param id 漫剧id
     * @return 漫剧信息、目录信息、创作者信息
     */
    @GetMapping("/comic/{id}")
    ComicFullInfoDTO getCourseInfoById(
            @PathVariable("id") Long id,
            @RequestParam(value = "withCatalogue", required = false) boolean withCatalogue,
            @RequestParam(value = "withTeachers", required = false) boolean withTeachers
    );
}
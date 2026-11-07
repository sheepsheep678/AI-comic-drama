package com.aicomic.api.client.comic;

import com.aicomic.api.dto.comic.EpisodeSimpleInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "episode", value = "comic-service",path = "episodes")
public interface EpisodeClient {

    /**
     * 根据目录id列表查询目录信息
     *
     * @param ids 目录id列表
     * @return id列表中对应的目录基础信息
     */
    @GetMapping("/batchQuery")
    List<EpisodeSimpleInfoDTO> batchQueryCatalogue(@RequestParam("ids") Iterable<Long> ids);


}
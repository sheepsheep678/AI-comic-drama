package com.aicomic.api.client.comic;

import com.aicomic.api.dto.comic.CategoryBasicDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(contextId = "category",value = "comic-service",path = "categories")
public interface CategoryClient {

    /**
     * 获取所有漫剧及漫剧分类
     * @return  所有漫剧及漫剧分类
     */
    @GetMapping("getAllOfOneLevel")
    List<CategoryBasicDTO> getAllOfOneLevel();
}

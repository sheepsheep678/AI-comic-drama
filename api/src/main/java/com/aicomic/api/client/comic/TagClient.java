package com.aicomic.api.client.comic;

import com.aicomic.api.dto.comic.TagDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "comic-service", path = "tags")
public interface TagClient {

    @GetMapping("list")
    List<TagDTO> queryByIds(@RequestParam("ids") Iterable<Long> ids);
}

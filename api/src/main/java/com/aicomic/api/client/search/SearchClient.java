package com.aicomic.api.client.search;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("search-service")
public interface SearchClient {

    @GetMapping("/comics/name")
    List<Long> queryCoursesIdByName(
            @RequestParam(value = "keyword", required = false) String keyword);
}

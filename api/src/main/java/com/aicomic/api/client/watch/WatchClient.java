package com.aicomic.api.client.watch;

import com.aicomic.api.client.watch.fallback.WatchClientFallback;
import com.aicomic.api.dto.watch.WatchEpisodeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "watch-service", fallbackFactory = WatchClientFallback.class)
public interface WatchClient {

    /**
     * 统计漫剧观看人数
     * @param courseId 漫剧id
     * @return 观看人数
     */
    @GetMapping("/episodes/{courseId}/count")
    Integer countWatchEpisodeByCourse(@PathVariable("courseId") Long courseId);

    /**
     * 校验当前用户是否可以观看当前漫剧
     * @param courseId 漫剧id
     * @return true：漫剧有效，false：漫剧无效，不能观看
     */
    @GetMapping("/episodes/{courseId}/valid")
    Long isLessonValid(@PathVariable("courseId") Long courseId);

    /**
     * 查询当前用户指定漫剧的观看进度
     * @param courseId 漫剧id
     * @return 追剧列表信息、观看记录及进度信息
     */
    @GetMapping("/watch-records/comic/{courseId}")
    WatchEpisodeDTO queryWatchRecordByCourse(@PathVariable("courseId") Long courseId);

}

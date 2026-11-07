package com.aicomic.api.client.watch.fallback;

import com.aicomic.api.client.watch.WatchClient;
import com.aicomic.api.dto.watch.WatchEpisodeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class WatchClientFallback implements FallbackFactory<WatchClient> {

    @Override
    public WatchClient create(Throwable cause) {
        log.error("查询观看服务异常", cause);
        return new WatchClient() {
            @Override
            public Integer countWatchEpisodeByCourse(Long courseId) {
                return 0;
            }

            @Override
            public Long isLessonValid(Long courseId) {
                return null;
            }

            @Override
            public WatchEpisodeDTO queryWatchRecordByCourse(Long courseId) {
                return null;
            }
        };
    }
}

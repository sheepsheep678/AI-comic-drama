package com.aicomic.api.client.order.fallback;

import com.aicomic.api.client.order.OrderClient;
import com.aicomic.api.dto.comic.ComicPurchaseInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class OrderClientFallback implements FallbackFactory<OrderClient> {

    @Override
    public OrderClient create(Throwable cause) {
        log.error("查询交易服务异常", cause);
        return new OrderClient() {

            @Override
            public Map<Long, Integer> countPurchaseNumOfComic(List<Long> courseIdList) {
                return new HashMap<>();
            }

            @Override
            public Map<Long, Integer> countPurchaseComicOfViewer(List<Long> viewerIds) {
                return new HashMap<>();
            }

            @Override
            public Boolean checkMyLesson(Long id) {
                return false;
            }

            @Override
            public ComicPurchaseInfoDTO getPurchaseInfoOfCourse(Long courseId) {
                return new ComicPurchaseInfoDTO();
            }
        };
    }
}

package com.aicomic.api.client.order;

import com.aicomic.api.client.order.fallback.OrderClientFallback;
import com.aicomic.api.dto.comic.ComicPurchaseInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "order-service", fallbackFactory = OrderClientFallback.class)
public interface OrderClient {
    /**
     * 统计指定漫剧的领取人数
     * @param courseIdList 漫剧id集合
     * @return 统计结果
     */
    @GetMapping("/order-details/purchaseNum")
    Map<Long, Integer> countPurchaseNumOfComic(@RequestParam("courseIdList") List<Long> courseIdList);

    /**
     * 统计指定观众的领取漫剧数量
     * @param viewerIds 观众id集合
     * @return 统计结果
     */
    @GetMapping("/order-details/purchaseComic")
    Map<Long, Integer> countPurchaseComicOfViewer(@RequestParam("viewerIds") List<Long> viewerIds);

    /**
     * 检查当前用户是否领取指定漫剧
     * @param id 漫剧id
     * @return 是否领取
     */
    @GetMapping("/order-details/comic/{id}")
    Boolean checkMyLesson(@PathVariable("id") Long id);

    /**
     * 统计漫剧购买、退款状态
     * @param courseId 漫剧id
     * @return 统计结果
     */
    @GetMapping("/order-details/purchaseInfo")
    ComicPurchaseInfoDTO getPurchaseInfoOfCourse(@RequestParam("courseId") Long courseId);
}

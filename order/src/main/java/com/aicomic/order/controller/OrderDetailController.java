package com.aicomic.order.controller;


import com.aicomic.api.dto.comic.ComicPurchaseInfoDTO;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.order.domain.query.OrderDetailPageQuery;
import com.aicomic.order.domain.vo.OrderDetailAdminVO;
import com.aicomic.order.domain.vo.OrderDetailPageVO;
import com.aicomic.order.service.IOrderDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单明细 前端控制器
 * </p>
 *
 * @since 2022-08-29
 */

@Api(tags = "订单明细相关接口")
@RestController
@RequestMapping("/order-details")
@RequiredArgsConstructor
public class OrderDetailController {

    private final IOrderDetailService detailService;

    @ApiOperation("分页查询订单明细")
    @GetMapping("/page")
    public PageDTO<OrderDetailPageVO> queryDetailForPage(OrderDetailPageQuery pageQuery) {
        return detailService.queryDetailForPage(pageQuery);
    }

    @ApiOperation("根据订单明细id获取详细信息")
    @GetMapping("/{id}")
    public OrderDetailAdminVO queryOrdersDetailProgress( @ApiParam(value = "订单明细id")@PathVariable("id") Long id) {
        return detailService.queryOrdersDetailProgress(id);
    }

    @ApiOperation("校验漫剧是否购买，是否过期")
    @GetMapping("/comic/{id}")
    public Boolean checkCourseOrderInfo(@PathVariable("id") Long courseId){
        return detailService.checkCourseOrderInfo(courseId);
    }

    @ApiOperation("统计漫剧领取人数")
    @GetMapping("/purchaseNum")
    public Map<Long, Integer> countPurchaseNumOfComic(@RequestParam("courseIdList") List<Long> courseIdList){
        return detailService.countPurchaseNumOfComic(courseIdList);
    }

    @ApiOperation("统计观众领取漫剧数量")
    @GetMapping("/purchaseComic")
    public Map<Long, Integer> countPurchaseComicOfViewer(@RequestParam("viewerIds") List<Long> viewerIds){
        return detailService.countPurchaseComicOfViewer(viewerIds);
    }

    @GetMapping("purchaseInfo")
    public ComicPurchaseInfoDTO getPurchaseInfoOfCourse(@RequestParam("courseId") Long courseId){
        return detailService.getPurchaseInfoOfCourse(courseId);
    }
}

package com.aicomic.api.client.unlock;

import com.aicomic.api.client.unlock.fallback.UnlockClientFallback;
import com.aicomic.api.dto.unlock.CouponDiscountDTO;
import com.aicomic.api.dto.unlock.OrderCouponDTO;
import com.aicomic.api.dto.unlock.OrderComicDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "unlock-service", fallbackFactory = UnlockClientFallback.class)
public interface UnlockClient {

    @PostMapping("/user-coupons/available")
    List<CouponDiscountDTO> findDiscountSolution(@RequestBody List<OrderComicDTO> orderCourses);

    @ApiOperation("根据券方案计算订单优惠明细")
    @PostMapping("/user-coupons/discount")
    CouponDiscountDTO queryDiscountDetailByOrder(@RequestBody OrderCouponDTO orderCouponDTO);

    @ApiOperation("核销指定优惠券")
    @PutMapping("/user-coupons/use")
    void writeOffCoupon(@ApiParam("用户优惠券id集合") @RequestParam("couponIds") List<Long> userCouponIds);

    @ApiOperation("退还指定优惠券")
    @PutMapping("/user-coupons/refund")
    void refundCoupon(@ApiParam("用户优惠券id集合") @RequestParam("couponIds") List<Long> userCouponIds);

    @ApiOperation("分页查询我的优惠券接口")
    @GetMapping("/user-coupons/rules")
    List<String> queryDiscountRules(@ApiParam("用户优惠券id集合") @RequestParam("couponIds") List<Long> userCouponIds);
}

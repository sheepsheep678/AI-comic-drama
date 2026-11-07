package com.aicomic.unlock.service;

import com.aicomic.api.dto.unlock.CouponDiscountDTO;
import com.aicomic.api.dto.unlock.OrderCouponDTO;
import com.aicomic.api.dto.unlock.OrderComicDTO;

import java.util.List;

public interface IDiscountService {
    List<CouponDiscountDTO> findDiscountSolution(List<OrderComicDTO> orderCourses);

    CouponDiscountDTO queryDiscountDetailByOrder(OrderCouponDTO orderCouponDTO);
}

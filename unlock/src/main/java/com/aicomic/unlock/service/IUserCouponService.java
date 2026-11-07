package com.aicomic.unlock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.unlock.domain.dto.UserCouponDTO;
import com.aicomic.unlock.domain.po.UserCoupon;
import com.aicomic.unlock.domain.query.UserCouponQuery;
import com.aicomic.unlock.domain.vo.CouponVO;

import java.util.List;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务类
 * </p>
 *
 */
public interface IUserCouponService extends IService<UserCoupon> {
    void receiveCoupon(Long couponId);

    void checkAndCreateUserCoupon(UserCouponDTO uc);

    void exchangeCoupon(String code);

    PageDTO<CouponVO> queryMyCouponPage(UserCouponQuery query);

    void writeOffCoupon(List<Long> userCouponIds);

    void refundCoupon(List<Long> userCouponIds);

    List<String> queryDiscountRules(List<Long> userCouponIds);
}

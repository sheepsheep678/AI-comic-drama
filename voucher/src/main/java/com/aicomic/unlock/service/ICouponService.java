package com.aicomic.unlock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.unlock.domain.dto.CouponFormDTO;
import com.aicomic.unlock.domain.dto.CouponIssueFormDTO;
import com.aicomic.unlock.domain.po.Coupon;
import com.aicomic.unlock.domain.query.CouponQuery;
import com.aicomic.unlock.domain.vo.CouponDetailVO;
import com.aicomic.unlock.domain.vo.CouponPageVO;
import com.aicomic.unlock.domain.vo.CouponVO;

import java.util.List;

/**
 * <p>
 * 优惠券的规则信息 服务类
 * </p>
 *
 */
public interface ICouponService extends IService<Coupon> {

    void saveCoupon(CouponFormDTO dto);

    PageDTO<CouponPageVO> queryCouponByPage(CouponQuery query);

    void beginIssue(CouponIssueFormDTO dto);

    List<CouponVO> queryIssuingCoupons();

    void pauseIssue(Long id);

    void deleteById(Long id);

    CouponDetailVO queryCouponById(Long id);

    void beginIssueBatch(List<Coupon> coupons);
}

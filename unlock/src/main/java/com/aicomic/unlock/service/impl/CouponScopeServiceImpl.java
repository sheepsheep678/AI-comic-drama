package com.aicomic.unlock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.unlock.domain.po.CouponScope;
import com.aicomic.unlock.mapper.CouponScopeMapper;
import com.aicomic.unlock.service.ICouponScopeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 优惠券作用范围信息 服务实现类
 * </p>
 *
 */
@Service
public class CouponScopeServiceImpl extends ServiceImpl<CouponScopeMapper, CouponScope> implements ICouponScopeService {
}

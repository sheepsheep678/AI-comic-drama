package com.aicomic.unlock.strategy.discount;

import com.aicomic.common.utils.NumberUtils;
import com.aicomic.common.utils.StringUtils;
import com.aicomic.unlock.domain.po.Coupon;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PriceDiscount implements Discount{

    private static final String RULE_TEMPLATE = "满{}减{}";


    @Override
    public boolean canUse(int totalAmount, Coupon coupon) {
        return totalAmount >= coupon.getThresholdAmount();
    }

    @Override
    public int calculateDiscount(int totalAmount, Coupon coupon) {
        return coupon.getDiscountValue();
    }

    @Override
    public String getRule(Coupon coupon) {
        return StringUtils.format(
                RULE_TEMPLATE,
                NumberUtils.scaleToStr(coupon.getThresholdAmount(), 2),
                NumberUtils.scaleToStr(coupon.getDiscountValue(), 2)
        );
    }
}

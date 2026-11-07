package com.aicomic.api.client.unlock.fallback;

import com.aicomic.api.client.unlock.UnlockClient;
import com.aicomic.api.dto.unlock.CouponDiscountDTO;
import com.aicomic.api.dto.unlock.OrderCouponDTO;
import com.aicomic.api.dto.unlock.OrderComicDTO;
import com.aicomic.common.exceptions.BizIllegalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collections;
import java.util.List;

@Slf4j
public class UnlockClientFallback implements FallbackFactory<UnlockClient> {
    @Override
    public UnlockClient create(Throwable cause) {
        log.error("查询解锁服务出现异常，", cause);
        return new UnlockClient() {
            @Override
            public List<CouponDiscountDTO> findDiscountSolution(List<OrderComicDTO> orderCourses) {
                return Collections.emptyList();
            }

            @Override
            public CouponDiscountDTO queryDiscountDetailByOrder(OrderCouponDTO orderCouponDTO) {
                return null;
            }

            @Override
            public void writeOffCoupon(List<Long> userCouponIds) {
                throw new BizIllegalException(500, "核销优惠券异常", cause);
            }

            @Override
            public void refundCoupon(List<Long> userCouponIds) {
                throw new BizIllegalException(500, "退还优惠券异常", cause);
            }

            @Override
            public List<String> queryDiscountRules(List<Long> userCouponIds) {
                return Collections.emptyList();
            }
        };
    }
}

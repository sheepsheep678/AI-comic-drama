package com.aicomic.unlock.service.impl;

import com.aicomic.api.dto.unlock.CouponDiscountDTO;
import com.aicomic.api.dto.unlock.OrderCouponDTO;
import com.aicomic.api.dto.unlock.OrderComicDTO;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.common.utils.UserContext;
import com.aicomic.unlock.domain.po.Coupon;
import com.aicomic.unlock.domain.po.CouponScope;
import com.aicomic.unlock.enums.UserCouponStatus;
import com.aicomic.unlock.mapper.UserCouponMapper;
import com.aicomic.unlock.service.ICouponScopeService;
import com.aicomic.unlock.service.IDiscountService;
import com.aicomic.unlock.strategy.discount.Discount;
import com.aicomic.unlock.strategy.discount.DiscountStrategy;
import com.aicomic.unlock.utils.PermuteUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements IDiscountService {

    private final UserCouponMapper userCouponMapper;

    private final ICouponScopeService scopeService;

    private final Executor discountSolutionExecutor;

    @Override
    public List<CouponDiscountDTO> findDiscountSolution(List<OrderComicDTO> orderCourses) {
        // 1.查询我的所有可用优惠券
        List<Coupon> coupons = userCouponMapper.queryMyCoupons(UserContext.getUser());
        if (CollUtils.isEmpty(coupons)) {
            return CollUtils.emptyList();
        }
        // 2.初筛
        // 2.1.计算订单总价
        int totalAmount = orderCourses.stream().mapToInt(OrderComicDTO::getPrice).sum();
        // 2.2.筛选可用券
        List<Coupon> availableCoupons = coupons.stream()
                .filter(c -> DiscountStrategy.getDiscount(c.getDiscountType()).canUse(totalAmount, c))
                .collect(Collectors.toList());
        if (CollUtils.isEmpty(availableCoupons)) {
            return CollUtils.emptyList();
        }
        // 3.排列组合出所有方案
        // 3.1.细筛（找出每一个优惠券的可用的漫剧，判断漫剧总价是否达到优惠券的使用需求）
        Map<Coupon, List<OrderComicDTO>> availableCouponMap = findAvailableCoupon(availableCoupons, orderCourses);
        if (CollUtils.isEmpty(availableCouponMap)) {
            return CollUtils.emptyList();
        }
        // 3.2.排列组合
        availableCoupons = new ArrayList<>(availableCouponMap.keySet());
        List<List<Coupon>> solutions = PermuteUtil.permute(availableCoupons);
        // 3.3.添加单券的方案
        for (Coupon c : availableCoupons) {
            solutions.add(List.of(c));
        }
        // 4.计算方案的优惠明细
        List<CouponDiscountDTO> list = Collections.synchronizedList(new ArrayList<>(solutions.size()));
        // 4.1.定义闭锁
        CountDownLatch latch = new CountDownLatch(solutions.size());
        for (List<Coupon> solution : solutions) {
            // 4.2.异步计算
            CompletableFuture
                    .supplyAsync(
                            () -> calculateSolutionDiscount(availableCouponMap, orderCourses, solution),
                            discountSolutionExecutor
                    ).thenAccept(dto -> {
                // 4.3.提交任务结果
                list.add(dto);
                latch.countDown();
            });
        }
        // 4.4.等待运算结束
        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("优惠方案计算被中断，{}", e.getMessage());
        }
        // 5.筛选最优解
        return findBestSolution(list);
    }

    @Override
    public CouponDiscountDTO queryDiscountDetailByOrder(OrderCouponDTO orderCouponDTO) {
        // 1.查询用户优惠券
        List<Long> userCouponIds = orderCouponDTO.getUserCouponIds();
        List<Coupon> coupons = userCouponMapper.queryCouponByUserCouponIds(userCouponIds, UserCouponStatus.UNUSED);
        if (CollUtils.isEmpty(coupons)) {
            return null;
        }
        // 2.查询优惠券对应漫剧
        Map<Coupon, List<OrderComicDTO>> availableCouponMap = findAvailableCoupon(coupons, orderCouponDTO.getCourseList());
        if (CollUtils.isEmpty(availableCouponMap)) {
            return null;
        }
        // 3.查询优惠券规则
        return calculateSolutionDiscount(availableCouponMap, orderCouponDTO.getCourseList(), coupons);
    }

    private List<CouponDiscountDTO> findBestSolution(List<CouponDiscountDTO> list) {
        // 1.准备Map记录最优解
        Map<String, CouponDiscountDTO> moreDiscountMap = new HashMap<>();
        Map<Integer, CouponDiscountDTO> lessCouponMap = new HashMap<>();
        // 2.遍历，筛选最优解
        for (CouponDiscountDTO solution : list) {
            // 2.1.计算当前方案的id组合
            String ids = solution.getIds().stream()
                    .sorted(Long::compare).map(String::valueOf).collect(Collectors.joining(","));
            // 2.2.比较用券相同时，优惠金额是否最大
            CouponDiscountDTO best = moreDiscountMap.get(ids);
            if (best != null && best.getDiscountAmount() >= solution.getDiscountAmount()) {
                // 当前方案优惠金额少，跳过
                continue;
            }
            // 2.3.比较金额相同时，用券数量是否最少
            best = lessCouponMap.get(solution.getDiscountAmount());
            int size = solution.getIds().size();
            if (size > 1 && best != null && best.getIds().size() <= size) {
                // 当前方案用券更多，放弃
                continue;
            }
            // 2.4.更新最优解
            moreDiscountMap.put(ids, solution);
            lessCouponMap.put(solution.getDiscountAmount(), solution);
        }
        // 3.求交集
        Collection<CouponDiscountDTO> bestSolutions = CollUtils
                .intersection(moreDiscountMap.values(), lessCouponMap.values());
        // 4.排序，按优惠金额降序
        return bestSolutions.stream()
                .sorted(Comparator.comparingInt(CouponDiscountDTO::getDiscountAmount).reversed())
                .collect(Collectors.toList());
    }

    private CouponDiscountDTO calculateSolutionDiscount(
            Map<Coupon, List<OrderComicDTO>> couponMap, List<OrderComicDTO> courses, List<Coupon> solution) {
        // 1.初始化DTO
        CouponDiscountDTO dto = new CouponDiscountDTO();
        // 2.初始化折扣明细的映射
        Map<Long, Integer> detailMap = courses.stream().collect(Collectors.toMap(OrderComicDTO::getId, oc -> 0));
        dto.setDiscountDetail(detailMap);
        // 3.计算折扣
        for (Coupon coupon : solution) {
            // 3.1.获取优惠券限定范围对应的漫剧
            List<OrderComicDTO> availableCourses = couponMap.get(coupon);
            // 3.2.计算漫剧总价(漫剧原价 - 折扣明细)
            int totalAmount = availableCourses.stream()
                    .mapToInt(oc -> oc.getPrice() - detailMap.get(oc.getId())).sum();
            // 3.3.判断是否可用
            Discount discount = DiscountStrategy.getDiscount(coupon.getDiscountType());
            if (!discount.canUse(totalAmount, coupon)) {
                // 券不可用，跳过
                continue;
            }
            // 3.4.计算优惠金额
            int discountAmount = discount.calculateDiscount(totalAmount, coupon);
            // 3.5.计算优惠明细
            calculateDiscountDetails(detailMap, availableCourses, totalAmount, discountAmount);
            // 3.6.更新DTO数据
            dto.getIds().add(coupon.getCreater());
            dto.getRules().add(discount.getRule(coupon));
            dto.setDiscountAmount(discountAmount + dto.getDiscountAmount());
        }
        return dto;
    }

    private void calculateDiscountDetails(Map<Long, Integer> detailMap, List<OrderComicDTO> courses,
                                          int totalAmount, int discountAmount) {
        int times = 0;
        int remainDiscount = discountAmount;
        for (OrderComicDTO course : courses) {
            // 更新漫剧已计算数量
            times++;
            int discount = 0;
            // 判断是否是最后一个漫剧
            if (times == courses.size()) {
                // 是最后一个漫剧，总折扣金额 - 之前所有商品的折扣金额之和
                discount = remainDiscount;
            } else {
                // 计算折扣明细（漫剧价格在总价中占的比例，乘以总的折扣）
                discount = discountAmount * course.getPrice() / totalAmount;
                remainDiscount -= discount;
            }
            // 更新折扣明细
            detailMap.put(course.getId(), discount + detailMap.get(course.getId()));
        }
    }

    private Map<Coupon, List<OrderComicDTO>> findAvailableCoupon(
            List<Coupon> coupons, List<OrderComicDTO> courses) {
        Map<Coupon, List<OrderComicDTO>> map = new HashMap<>(coupons.size());
        for (Coupon coupon : coupons) {
            // 1.找出优惠券的可用的漫剧
            List<OrderComicDTO> availableCourses = courses;
            if (coupon.getSpecific()) {
                // 1.1.限定了范围，查询券的可用范围
                List<CouponScope> scopes = scopeService.lambdaQuery().eq(CouponScope::getCouponId, coupon.getId()).list();
                // 1.2.获取范围对应的分类id
                Set<Long> scopeIds = scopes.stream().map(CouponScope::getBizId).collect(Collectors.toSet());
                // 1.3.筛选漫剧
                availableCourses = courses.stream()
                        .filter(c -> scopeIds.contains(c.getCateId())).collect(Collectors.toList());
            }
            if (CollUtils.isEmpty(availableCourses)) {
                // 没有任何可用漫剧，抛弃
                continue;
            }
            // 2.计算漫剧总价
            int totalAmount = availableCourses.stream().mapToInt(OrderComicDTO::getPrice).sum();
            // 3.判断是否可用
            Discount discount = DiscountStrategy.getDiscount(coupon.getDiscountType());
            if (discount.canUse(totalAmount, coupon)) {
                map.put(coupon, availableCourses);
            }
        }
        return map;
    }
}

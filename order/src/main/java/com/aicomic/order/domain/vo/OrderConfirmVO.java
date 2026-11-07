package com.aicomic.order.domain.vo;

import com.aicomic.api.dto.unlock.CouponDiscountDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "订单确认页信息")
public class OrderConfirmVO {
    @ApiModelProperty("订单id")
    private Long orderId;
    @ApiModelProperty("订单总金额")
    private Integer totalAmount;
    @ApiModelProperty("优惠折扣方案")
    private List<CouponDiscountDTO> discounts;
    @ApiModelProperty("订单中包含的漫剧")
    private List<OrderComicVO> courses;
}

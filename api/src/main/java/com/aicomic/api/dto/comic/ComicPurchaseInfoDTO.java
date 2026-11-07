package com.aicomic.api.dto.comic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 漫剧支付相关信息 漫剧状态
 * @since 2022/7/26 20:41
 * @version 1.0.0
 **/
@Data
@ApiModel("漫剧购买信息")
@NoArgsConstructor
@AllArgsConstructor
public class ComicPurchaseInfoDTO {
    @ApiModelProperty("领取人数")
    private Integer purchaseNum;
    @ApiModelProperty("退款人数")
    private Integer refundNum;
    @ApiModelProperty("实付总金额")
    private Integer realPayAmount;
}

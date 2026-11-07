package com.aicomic.comic.domain.dto;

import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.utils.DateUtils;
import com.aicomic.common.validate.Checker;
import com.aicomic.comic.constants.ComicErrorInfo;
import com.aicomic.comic.utils.ComicSaveBaseGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 漫剧基本信息
 *
 * @ClassName ComicBaseInfoSaveDTO
 * @since 2022/7/11 11:39
 * @version 1.0.0
 **/

@Data
@ApiModel(description = "漫剧基本信息保存")
public class ComicBaseInfoSaveDTO implements Checker {
    @ApiModelProperty("漫剧id，新漫剧该值不能传，老漫剧必填")
    private Long id;
    @ApiModelProperty("漫剧名称")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_SAVE_NAME_NULL)
    private String name;
    @ApiModelProperty("三级漫剧分类id")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_SAVE_CATEGORY_NULL)
    private Long thirdCateId;
    @ApiModelProperty("封面链接url")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_SAVE_COVER_URL_NULL, groups = ComicSaveBaseGroup.class)
    private String coverUrl;
    @ApiModelProperty("是否是免费")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_SAVE_FREE_NULL)
    private Boolean free;
    @ApiModelProperty("漫剧价格")
    @Min(value = 0, message = ComicErrorInfo.Msg.COMIC_SAVE_PRICE_NEGATIVE)
    private Integer price;
//    @ApiModelProperty("购买周期开始时间")
//    @NotNull(message = ComicErrorInfo.Msg.COMIC_SAVE_PURCHASE_TIME_NULL)
    private LocalDateTime purchaseStartTime;
    @ApiModelProperty("购买周期结束时间")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_SAVE_PURCHASE_TIME_NULL)
    private LocalDateTime purchaseEndTime;
    @ApiModelProperty("漫剧介绍")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_SAVE_INTRODUCE_NULL, groups = ComicSaveBaseGroup.class)
    private String introduce;
    @ApiModelProperty("使用人群")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_SAVE_USE_PEOPLE_NULL, groups = ComicSaveBaseGroup.class)
    private String usePeople;
    @ApiModelProperty("详情")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_SAVE_DETAIL_NULL, groups = ComicSaveBaseGroup.class)
    private String detail;
    @ApiModelProperty("观看周期，0或不传表示没有期限，其他表示月数")
    @NotNull(message = ComicErrorInfo.Msg.COMIC_SAVE_DURATION_NULL)
    private Integer validDuration;

    @Override
    public void check() {
        if(!free) { //非免费
            if(price == null) { //付费漫剧未设置价格
                throw new BadRequestException(ComicErrorInfo.Msg.COMIC_SAVE_PRICE_NULL);
            }
            if(price <= 0){ //付费漫剧设置价格小于0
                throw new BadRequestException(ComicErrorInfo.Msg.COMIC_SAVE_PRICE_NEGATIVE);
            }
        }else { //免费
            if(price != null && price > 0){ //免费漫剧设置了价格
                throw new BadRequestException(ComicErrorInfo.Msg.COMIC_SAVE_PRICE_FREE);
            }
        }
        if(purchaseEndTime.isBefore(DateUtils.now())){
            throw new BadRequestException(ComicErrorInfo.Msg.COMIC_SAVE_PURCHASE_ILLEGAL);
        }
//        if (purchaseStartTime.isAfter(purchaseEndTime)) {
//            throw new BadRequestException(ComicErrorInfo.Msg.COMIC_SAVE_PURCHASE_ILLEGAL);
//        }
//        if(id == null && purchaseStartTime.isBefore(LocalDateTime.now())){
//            throw new BadRequestException(ComicErrorInfo.Msg.COMIC_SAVE_PURCHASE_ILLEGAL2);
//        }
    }
}

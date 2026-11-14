package com.aicomic.watch.domain.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicomic.common.domain.dto.PageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "观看计划分页统计结果")
public class WatchPlanPageVO extends PageDTO<WatchPlanVO> {
    @ApiModelProperty("本周积分值")
    private Integer weekPoints;
    @ApiModelProperty("本周完成的计划数量")
    private Integer weekFinished;
    @ApiModelProperty("总的计划观看数量")
    private Integer weekTotalPlan;

    public WatchPlanPageVO() {
    }

    public WatchPlanPageVO pageInfo(Long total, Long pages, List<WatchPlanVO> list) {
        this.total = total;
        this.pages = pages;
        this.list = list;
        return this;
    }

    public WatchPlanPageVO emptyPage(Page<?> page) {
        this.total = page.getTotal();
        this.pages = page.getPages();
        return this;
    }

}
package com.aicomic.data.service;


import com.aicomic.data.model.dto.TodayDataDTO;
import com.aicomic.data.model.vo.TodayDataVO;

/**
 * @since 2022/10/13 9:27
 **/
public interface TodayDataService {

    /**
     * 获取今日数据
     * @return
     */
    TodayDataVO get();

    /**
     * 设置今日数据
     * @param todayDataDTO
     */
    void set(TodayDataDTO todayDataDTO);
}
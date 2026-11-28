package com.aicomic.watch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.watch.domain.po.PointsRecord;
import com.aicomic.watch.domain.vo.PointsStatisticsVO;
import com.aicomic.watch.enums.PointsRecordType;

import java.util.List;

/**
 * <p>
 * 观看积分记录，每个月底清零 服务类
 * </p>
 *
 */
public interface IPointsRecordService extends IService<PointsRecord> {
    void addPointsRecord(Long userId, int points, PointsRecordType type);

    List<PointsStatisticsVO> queryMyPointsToday();

}

package com.aicomic.watch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.api.dto.watch.WatchEpisodeDTO;
import com.aicomic.watch.domain.dto.WatchRecordFormDTO;
import com.aicomic.watch.domain.po.WatchRecord;

/**
 * <p>
 * 观看记录表 服务类
 * </p>
 *
 * @since 2022-12-10
 */
public interface IWatchRecordService extends IService<WatchRecord> {

    WatchEpisodeDTO queryWatchRecordByCourse(Long courseId);

    void addWatchRecord(WatchRecordFormDTO formDTO);
}

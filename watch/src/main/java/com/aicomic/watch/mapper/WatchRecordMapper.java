package com.aicomic.watch.mapper;

import com.aicomic.api.dto.IdAndNumDTO;
import com.aicomic.watch.domain.po.WatchRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 观看记录表 Mapper 接口
 * </p>
 *
 * @since 2022-12-10
 */
public interface WatchRecordMapper extends BaseMapper<WatchRecord> {

    List<IdAndNumDTO> countLearnedSections(
            @Param("userId") Long userId,
            @Param("begin") LocalDateTime begin,
            @Param("end") LocalDateTime end);
}

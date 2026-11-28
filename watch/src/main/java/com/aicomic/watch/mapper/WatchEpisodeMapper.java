package com.aicomic.watch.mapper;

import com.aicomic.watch.domain.po.WatchEpisode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 观众漫剧表 Mapper 接口
 * </p>
 *
 * @since 2022-12-02
 */
public interface WatchEpisodeMapper extends BaseMapper<WatchEpisode> {

    Integer queryTotalPlan(@Param("userId") Long userId);
}

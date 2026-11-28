package com.aicomic.watch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.watch.domain.po.PointsBoard;
import com.aicomic.watch.domain.query.PointsBoardQuery;
import com.aicomic.watch.domain.vo.PointsBoardVO;

import java.util.List;

/**
 * <p>
 * 学霸天梯榜 服务类
 * </p>
 *
 */
public interface IPointsBoardService extends IService<PointsBoard> {
    PointsBoardVO queryPointsBoardBySeason(PointsBoardQuery query);

    void createPointsBoardTableBySeason(Integer season);

    List<PointsBoard> queryCurrentBoardList(String key, Integer pageNo, Integer pageSize);
}

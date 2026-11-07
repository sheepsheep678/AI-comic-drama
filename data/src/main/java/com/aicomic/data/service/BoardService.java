package com.aicomic.data.service;


import com.aicomic.data.model.dto.BoardDataSetDTO;
import com.aicomic.data.model.vo.EchartsVO;

import java.util.List;

/**
 * @ClassName BoardService
 * @Author 
 * @Date 2022/10/10 16:30
 * @Version
 **/
public interface BoardService {

    /**
     * 看板数据获取
     *
     * @param types 数据类型
     * @return
     */
    EchartsVO boardData(List<Integer> types);

    /**
     * 设置看板数据
     *
     * @param boardDataSetDTO
     */
    void setBoardData(BoardDataSetDTO boardDataSetDTO);
}
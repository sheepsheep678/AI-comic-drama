package com.aicomic.data.model.vo;

import com.aicomic.data.model.po.ComicInfo;
import lombok.Data;

import java.util.List;

/**
 * @ClassName Top10DataVO
 * @Author 
 * @Date 2022/10/10 19:33
 * @Version
 **/
@Data
public class Top10DataVO {
    // 热门漫剧
    private List<ComicInfo> hot;
    // 热销漫剧
    private List<ComicInfo> hotSales;
}

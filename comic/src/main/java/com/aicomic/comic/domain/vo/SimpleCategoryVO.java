package com.aicomic.comic.domain.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 所有漫剧分类数据
 * @since 2022/7/14 18:15
 * @version 1.0.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "所有漫剧分类数据")
public class SimpleCategoryVO {
    private Long id;
    private String name;
    private List<SimpleCategoryVO> children;
    private Integer level;
    private Long parentId;
}

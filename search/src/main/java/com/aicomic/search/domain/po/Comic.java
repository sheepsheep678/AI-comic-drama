package com.aicomic.search.domain.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Comic {
    @Id
    private Long id;
    /** 漫剧名称 */
    private String name;
    /** 分类id */
    private Long categoryIdLv1;
    /** 分类id2 */
    private Long categoryIdLv2;
    /** 分类id3 */
    private Long categoryIdLv3;
    /** 是否免费 */
    private Boolean free;
    /** 漫剧类型：1：直播课，2：录播课 */
    private Integer type;
    /** 漫剧销量，领取人数 */
    private Integer sold;
    /** 价格 */
    private Integer price;
    /** 漫剧评分 */
    private Integer score;
    /** 创作者id */
    private Long creator;
    /** 章节数量 */
    private Integer sections;
    /** 漫剧封面 */
    private String coverUrl;
    private LocalDateTime publishTime;

    @JsonIgnore
    public List<Long> getCategoryIds(){
        return List.of(categoryIdLv1, categoryIdLv2, categoryIdLv3);
    }
}

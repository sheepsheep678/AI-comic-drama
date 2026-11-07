package com.aicomic.comic.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName ComicStatus
 * @Author 
 * @Date 2022/7/18 16:07
 * @Version
 **/
@Getter
@AllArgsConstructor
public enum ComicStatus {
    NO_UP_SHELF(1, "待上架"),
    SHELF(2, "已上架"),
    DOWN_SHELF(3, "下架"),
    FINISHED(4, "已完结");
    private Integer status;
    private String desc;

    public static String desc(Integer status) {
        for (ComicStatus courseStatus : values()) {
            if (courseStatus.getStatus() == status) {
                return courseStatus.getDesc();
            }
        }
        return null;
    }

    public boolean equals(Integer status){
        return this.status.intValue() == status;
    }
}
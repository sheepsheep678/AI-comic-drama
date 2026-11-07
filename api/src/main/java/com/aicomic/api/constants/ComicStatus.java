package com.aicomic.api.constants;

import com.aicomic.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @since 2022/7/18 16:07
 * @version 1.0.0
 **/
@Getter
@AllArgsConstructor
public enum ComicStatus implements BaseEnum {
    NO_UP_SHELF(1, "待上架"),
    SHELF(2, "已上架"),
    DOWN_SHELF(3, "下架"),
    FINISHED(4, "已完结");

    private final int value;
    private final String desc;

    public static String desc(Integer status) {
        if (status == null) {
            return "";
        }
        for (ComicStatus courseStatus : values()) {
            if (courseStatus.getValue() == status) {
                return courseStatus.getDesc();
            }
        }
        return null;
    }
}
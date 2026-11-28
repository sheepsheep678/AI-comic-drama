package com.aicomic.watch.enums;

import com.aicomic.common.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 管理端评论状态
 */
@Getter
public enum CommentStatus implements BaseEnum {
    UN_CHECK(0, "未查看"),
    CHECKED(1, "已查看"),
    ;
    @JsonValue
    @EnumValue
    int value;
    String desc;

    CommentStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CommentStatus of(Integer value) {
        if (value == null) {
            return null;
        }
        for (CommentStatus status : values()) {
            if (status.equalsValue(value)) {
                return status;
            }
        }
        return null;
    }
}

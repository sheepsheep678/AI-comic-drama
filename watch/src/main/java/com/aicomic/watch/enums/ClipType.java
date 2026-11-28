package com.aicomic.watch.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.aicomic.common.enums.BaseEnum;
import lombok.Getter;

@Getter
public enum ClipType implements BaseEnum {
    VIDEO(1, "视频"),
    QUIZ(2, "答题"),
    ;
    @JsonValue
    @EnumValue
    int value;
    String desc;

    ClipType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ClipType of(Integer value){
        if (value == null) {
            return null;
        }
        for (ClipType status : values()) {
            if (status.equalsValue(value)) {
                return status;
            }
        }
        return null;
    }
}

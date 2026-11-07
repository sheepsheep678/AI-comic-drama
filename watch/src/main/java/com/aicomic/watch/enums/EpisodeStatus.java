package com.aicomic.watch.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.aicomic.common.enums.BaseEnum;
import lombok.Getter;

@Getter
public enum EpisodeStatus implements BaseEnum {
    NOT_BEGIN(0, "未观看"),
    WATCHING(1, "观看中"),
    FINISHED(2, "已学完"),
    EXPIRED(3, "已过期"),
    ;
    @JsonValue
    @EnumValue
    int value;
    String desc;

    EpisodeStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EpisodeStatus of(Integer value){
        if (value == null) {
            return null;
        }
        for (EpisodeStatus status : values()) {
            if (status.equalsValue(value)) {
                return status;
            }
        }
        return null;
    }
}

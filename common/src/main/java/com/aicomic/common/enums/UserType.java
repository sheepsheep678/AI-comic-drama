package com.aicomic.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.aicomic.common.constants.ErrorInfo;
import com.aicomic.common.exceptions.BadRequestException;
import lombok.Getter;

@Getter
public enum UserType implements BaseEnum{
    ADMIN(1, "管理员"),
    VIEWER(2, "观众"),
    CREATOR(3, "创作者"),
    ;
    @EnumValue
    int value;
    String desc;

    UserType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static UserType of(int value) {
        for (UserType type : UserType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new BadRequestException(ErrorInfo.Msg.INVALID_USER_TYPE);
    }
}

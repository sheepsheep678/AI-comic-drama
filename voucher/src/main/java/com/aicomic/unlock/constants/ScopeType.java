package com.aicomic.unlock.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.aicomic.common.enums.BaseEnum;
import com.aicomic.unlock.strategy.scope.CategoryScope;
import com.aicomic.unlock.strategy.scope.ComicScope;
import com.aicomic.unlock.strategy.scope.NoScope;
import com.aicomic.unlock.strategy.scope.Scope;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ScopeType implements BaseEnum {
    ALL(0, "全部"){
        @Override
        public Scope buildScope(List<Long> scopeIds) {
            return new NoScope();
        }
    },
    CATEGORY(1, "指定分类"){
        @Override
        public Scope buildScope(List<Long> scopeIds) {
            return new CategoryScope(scopeIds);
        }
    },
    COMIC(2, "指定漫剧") {
        @Override
        public Scope buildScope(List<Long> scopeIds) {
            return new ComicScope(scopeIds);
        }
    },
    ;
    private final int value;
    private final String desc;

    public static final String CATEGORY_HANDLER_NAME = "CATEGORY";
    public static final String COMIC_HANDLER_NAME = "COMIC";

    @JsonCreator
    public static ScopeType of(Integer value) {
        if (value == null) {
            return null;
        }
        for (ScopeType status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }

    public static String desc(Integer value) {
        ScopeType status = of(value);
        return status == null ? "" : status.desc;
    }

    public abstract Scope buildScope(List<Long> scopeIds);
}

package com.aicomic.unlock.strategy.scope;

import com.aicomic.api.dto.unlock.OrderComicDTO;
import com.aicomic.unlock.constants.ScopeType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ComicScope implements Scope {

    private final List<Long> scopeIds;

    @Override
    public boolean canUse(OrderComicDTO course) {
        return scopeIds.contains(course.getId());
    }

    @Override
    public ScopeType getType() {
        return ScopeType.COMIC;
    }
}

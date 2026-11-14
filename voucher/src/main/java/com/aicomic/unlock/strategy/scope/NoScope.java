package com.aicomic.unlock.strategy.scope;

import com.aicomic.api.dto.unlock.OrderComicDTO;
import com.aicomic.unlock.constants.ScopeType;

import java.util.List;

public class NoScope implements Scope{

    @Override
    public boolean canUse(OrderComicDTO course) {
        return true;
    }

    @Override
    public ScopeType getType() {
        return ScopeType.ALL;
    }

    @Override
    public List<Long> getScopeIds() {
        return null;
    }

}

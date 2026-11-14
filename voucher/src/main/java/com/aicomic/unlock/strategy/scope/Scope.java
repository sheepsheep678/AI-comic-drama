package com.aicomic.unlock.strategy.scope;

import com.aicomic.api.dto.unlock.OrderComicDTO;
import com.aicomic.unlock.constants.ScopeType;

import java.util.List;

public interface Scope {

    boolean canUse(OrderComicDTO course);

    ScopeType getType();

    List<Long> getScopeIds();
}

package com.aicomic.unlock.strategy.scope;

import java.util.List;

public interface ScopeNameHandler {
    List<String> getNameByIds(List<Long> scopeIds);
}

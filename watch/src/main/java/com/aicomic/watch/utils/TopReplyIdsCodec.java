package com.aicomic.watch.utils;

import com.aicomic.watch.constants.HotReplyConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * top_reply_ids 字段的编解码
 */
public final class TopReplyIdsCodec {

    /**
     * 已计算但没有可见回答
     */
    public static final String EMPTY = "";

    private TopReplyIdsCodec() {
    }

    /**
     * @return null 表示字段未计算；空列表表示已计算但无可见回答
     */
    public static List<Long> parse(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (String token : value.split(",")) {
            if (ids.size() >= HotReplyConstants.TOP_REPLY_SIZE) {
                break;
            }
            try {
                long id = Long.parseLong(token.trim());
                if (id > 0) {
                    ids.add(id);
                }
            } catch (NumberFormatException ignored) {
                // 忽略非法片段
            }
        }
        return new ArrayList<>(ids);
    }

    /**
     * 空集合编码为空串，表示已计算但无结果
     */
    public static String format(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return EMPTY;
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .limit(HotReplyConstants.TOP_REPLY_SIZE)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}

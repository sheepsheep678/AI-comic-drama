package com.aicomic.watch.utils;

import com.aicomic.watch.domain.dto.HotReplyCandidate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 按点赞量选取回答，纯逻辑，便于单元测试
 */
public final class TopReplySelector {

    private TopReplySelector() {
    }

    /**
     * 按点赞量倒序取前 n 条；同分按回复数、回答时间、id 倒序决胜，隐藏回答不参与
     */
    public static List<HotReplyCandidate> topNByLikes(Collection<HotReplyCandidate> candidates, int n) {
        if (candidates == null || candidates.isEmpty() || n <= 0) {
            return Collections.emptyList();
        }
        return candidates.stream()
                .filter(Objects::nonNull)
                .filter(c -> !Boolean.TRUE.equals(c.getHidden()))
                .sorted(comparator())
                .limit(n)
                .collect(Collectors.toList());
    }

    public static Comparator<HotReplyCandidate> comparator() {
        return Comparator
                .comparingInt((HotReplyCandidate c) -> normalize(c.getLikedTimes()))
                .reversed()
                .thenComparing(Comparator
                        .comparingInt((HotReplyCandidate c) -> normalize(c.getReplyTimes()))
                        .reversed())
                .thenComparing(HotReplyCandidate::getCreateTime,
                        Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()))
                .thenComparing(HotReplyCandidate::getReplyId,
                        Comparator.nullsLast(Comparator.<Long>reverseOrder()));
    }

    private static int normalize(Integer value) {
        return value == null || value < 0 ? 0 : value;
    }
}

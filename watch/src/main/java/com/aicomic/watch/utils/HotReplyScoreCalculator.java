package com.aicomic.watch.utils;

import com.aicomic.watch.constants.HotReplyConstants;
import com.aicomic.watch.domain.dto.HotReplyCandidate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 回答热度计算：点赞数、回复数和时间衰减
 */
public final class HotReplyScoreCalculator {

    private HotReplyScoreCalculator() {
    }

    /**
     * 计算热度分：likedTimes * 10 + replyTimes * 2 + 时间衰减
     */
    public static double score(Integer likedTimes, Integer replyTimes,
                               LocalDateTime createTime, LocalDateTime now) {
        int likes = normalize(likedTimes);
        int replies = normalize(replyTimes);
        double decay = 0.0;
        if (createTime != null && now != null) {
            long ageSeconds = Duration.between(createTime, now).getSeconds();
            if (ageSeconds < 0) {
                // 时钟漂移或未来的回答时间，按最新处理
                ageSeconds = 0;
            }
            decay = HotReplyConstants.MAX_DECAY_BONUS
                    * Math.pow(0.5, ageSeconds / (double) HotReplyConstants.HALF_LIFE_SECONDS);
        }
        return likes * HotReplyConstants.LIKED_WEIGHT
                + replies * HotReplyConstants.REPLY_WEIGHT
                + decay;
    }

    public static double score(HotReplyCandidate candidate, LocalDateTime now) {
        if (candidate == null) {
            return 0.0;
        }
        return score(candidate.getLikedTimes(), candidate.getReplyTimes(),
                candidate.getCreateTime(), now);
    }

    /**
     * 取热度最高的 n 条回答，隐藏回答不参与排名
     */
    public static List<HotReplyCandidate> topN(Collection<HotReplyCandidate> candidates,
                                               int n, LocalDateTime now) {
        if (candidates == null || candidates.isEmpty() || n <= 0) {
            return Collections.emptyList();
        }
        return candidates.stream()
                .filter(Objects::nonNull)
                .filter(c -> !Boolean.TRUE.equals(c.getHidden()))
                .sorted(comparator(now))
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * 热度倒序；同分时按点赞数、回复数、回答时间、id 倒序决胜
     */
    public static Comparator<HotReplyCandidate> comparator(LocalDateTime now) {
        return Comparator
                .comparingDouble((HotReplyCandidate c) -> score(c, now))
                .reversed()
                .thenComparing(Comparator
                        .comparingInt((HotReplyCandidate c) -> normalize(c.getLikedTimes()))
                        .reversed())
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

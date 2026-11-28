package com.aicomic.watch.utils;

import com.aicomic.watch.domain.dto.HotReplyCandidate;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 点赞 Top4 选择逻辑纯单元测试，不依赖 Spring/MySQL/Redis
 */
class TopReplySelectorTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void shouldOrderByLikesDescAndLimit() {
        List<HotReplyCandidate> result = TopReplySelector.topNByLikes(Arrays.asList(
                candidate(1, 3, NOW),
                candidate(2, 10, NOW),
                candidate(3, 5, NOW),
                candidate(4, 1, NOW),
                candidate(5, 8, NOW)
        ), 4);

        assertEquals(4, result.size());
        assertEquals(Arrays.asList(2L, 5L, 3L, 1L), ids(result));
    }

    @Test
    void shouldExcludeHidden() {
        HotReplyCandidate hidden = candidate(1, 100, NOW);
        hidden.setHidden(true);

        List<HotReplyCandidate> result = TopReplySelector.topNByLikes(
                Arrays.asList(hidden, candidate(2, 1, NOW)), 4);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getReplyId());
    }

    @Test
    void shouldBreakTieByCreateTimeThenId() {
        // 点赞相同，评论时间新者优先
        List<HotReplyCandidate> byTime = TopReplySelector.topNByLikes(Arrays.asList(
                candidate(1, 5, NOW.minusDays(2)),
                candidate(2, 5, NOW.minusDays(1))
        ), 4);
        assertEquals(2L, byTime.get(0).getReplyId());

        // 点赞、时间相同，id 大者优先
        List<HotReplyCandidate> byId = TopReplySelector.topNByLikes(Arrays.asList(
                candidate(1, 5, NOW),
                candidate(2, 5, NOW)
        ), 4);
        assertEquals(2L, byId.get(0).getReplyId());
    }

    @Test
    void shouldTreatNullLikesAsZero() {
        HotReplyCandidate nullLikes = candidate(1, 0, NOW);
        nullLikes.setLikedTimes(null);

        List<HotReplyCandidate> result = TopReplySelector.topNByLikes(
                Arrays.asList(nullLikes, candidate(2, 1, NOW)), 4);

        assertEquals(2L, result.get(0).getReplyId());
    }

    @Test
    void shouldHandleEmptyAndInvalidArguments() {
        assertTrue(TopReplySelector.topNByLikes(null, 4).isEmpty());
        assertTrue(TopReplySelector.topNByLikes(Collections.emptyList(), 4).isEmpty());
        assertTrue(TopReplySelector.topNByLikes(
                Collections.singletonList(candidate(1, 1, NOW)), 0).isEmpty());
    }

    private static List<Long> ids(List<HotReplyCandidate> candidates) {
        return candidates.stream().map(HotReplyCandidate::getReplyId).collect(Collectors.toList());
    }

    private static HotReplyCandidate candidate(long id, int likes, LocalDateTime createTime) {
        return new HotReplyCandidate()
                .setReplyId(id)
                .setCommentId(100L)
                .setLikedTimes(likes)
                .setCreateTime(createTime)
                .setHidden(false);
    }
}

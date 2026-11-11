package com.aicomic.watch.utils;

import com.aicomic.watch.domain.dto.HotReplyCandidate;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 热度计算纯单元测试，不依赖 Spring/MySQL/Redis
 */
class HotReplyScoreCalculatorTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void shouldScoreLikesRepliesAndNewReplyBonus() {
        assertEquals(20.0, HotReplyScoreCalculator.score(0, 0, NOW, NOW), 0.0001);
        assertEquals(30.0, HotReplyScoreCalculator.score(1, 0, NOW, NOW), 0.0001);
        assertEquals(22.0, HotReplyScoreCalculator.score(0, 1, NOW, NOW), 0.0001);
        assertEquals(32.0, HotReplyScoreCalculator.score(1, 1, NOW, NOW), 0.0001);
    }

    @Test
    void shouldDecayByHalfEveryHalfLife() {
        assertEquals(10.0, HotReplyScoreCalculator.score(0, 0, NOW.minusHours(72), NOW), 0.0001);
        assertEquals(5.0, HotReplyScoreCalculator.score(0, 0, NOW.minusHours(144), NOW), 0.0001);
    }

    @Test
    void shouldHandleNullAndInvalidValues() {
        assertEquals(0.0, HotReplyScoreCalculator.score(0, 0, null, NOW), 0.0001);
        assertEquals(20.0, HotReplyScoreCalculator.score(0, 0, NOW.plusHours(1), NOW), 0.0001);
        assertEquals(0.0, HotReplyScoreCalculator.score(-1, -1, null, NOW), 0.0001);
        assertEquals(0.0, HotReplyScoreCalculator.score(null, null, null, NOW), 0.0001);
    }

    @Test
    void shouldOrderByScoreAndExcludeHidden() {
        HotReplyCandidate low = candidate(1, 0, 0, null);
        HotReplyCandidate high = candidate(2, 3, 0, null);
        HotReplyCandidate hidden = candidate(3, 100, 100, NOW);
        hidden.setHidden(true);

        List<HotReplyCandidate> top = HotReplyScoreCalculator.topN(
                Arrays.asList(low, high, hidden), 2, NOW);

        assertEquals(2, top.size());
        assertEquals(2L, top.get(0).getReplyId());
        assertEquals(1L, top.get(1).getReplyId());
    }

    @Test
    void shouldBreakTiesByReplyIdDesc() {
        HotReplyCandidate first = candidate(1, 0, 0, null);
        HotReplyCandidate second = candidate(2, 0, 0, null);

        List<HotReplyCandidate> top = HotReplyScoreCalculator.topN(
                Arrays.asList(first, second), 2, NOW);

        assertEquals(2L, top.get(0).getReplyId());
        assertEquals(1L, top.get(1).getReplyId());
    }

    @Test
    void shouldReturnEmptyForEmptyOrInvalidInput() {
        assertTrue(HotReplyScoreCalculator.topN(null, 3, NOW).isEmpty());
        assertTrue(HotReplyScoreCalculator.topN(Collections.emptyList(), 3, NOW).isEmpty());
        assertTrue(HotReplyScoreCalculator.topN(
                Collections.singletonList(candidate(1, 1, 1, NOW)), 0, NOW).isEmpty());
    }

    private static HotReplyCandidate candidate(long id, int likes, int replies, LocalDateTime createTime) {
        return new HotReplyCandidate()
                .setReplyId(id)
                .setQuestionId(100L)
                .setLikedTimes(likes)
                .setReplyTimes(replies)
                .setCreateTime(createTime)
                .setHidden(false);
    }
}

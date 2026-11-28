package com.aicomic.watch.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * top_reply_ids 字段编解码纯单元测试
 */
class TopReplyIdsCodecTest {

    @Test
    void shouldParseNullAsUncomputed() {
        assertNull(TopReplyIdsCodec.parse(null));
    }

    @Test
    void shouldParseBlankAsComputedEmpty() {
        assertEquals(Collections.emptyList(), TopReplyIdsCodec.parse(""));
        assertEquals(Collections.emptyList(), TopReplyIdsCodec.parse("   "));
    }

    @Test
    void shouldParseCsvAndIgnoreInvalidValues() {
        assertEquals(Arrays.asList(1L, 2L, 3L),
                TopReplyIdsCodec.parse(" 1, 2 ,x,-1,3,3 "));
    }

    @Test
    void shouldTruncateToTop4() {
        assertEquals(Arrays.asList(1L, 2L, 3L, 4L),
                TopReplyIdsCodec.parse("1,2,3,4,5"));
    }

    @Test
    void shouldReturnNullForDirtyNonEmptyValue() {
        assertNull(TopReplyIdsCodec.parse("0,abc"));
    }

    @Test
    void shouldFormatIds() {
        assertEquals("", TopReplyIdsCodec.format(null));
        assertEquals("", TopReplyIdsCodec.format(Collections.emptyList()));
        assertEquals("1,2,3,4",
                TopReplyIdsCodec.format(Arrays.asList(1L, null, 2L, 1L, 3L, 4L, 5L)));
    }

    @Test
    void shouldRoundTrip() {
        String value = TopReplyIdsCodec.format(Arrays.asList(11L, 22L));
        assertEquals(Arrays.asList(11L, 22L), TopReplyIdsCodec.parse(value));
    }
}

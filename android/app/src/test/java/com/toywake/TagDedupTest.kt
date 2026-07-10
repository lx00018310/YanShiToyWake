package com.toywake

import com.toywake.nfc.TagDedup
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TagDedupTest {

    @Test
    fun first_read_processes() {
        val d = TagDedup(windowMs = 800) { 1000L }
        assertTrue(d.shouldProcess("ABC"))
    }

    @Test
    fun same_uid_within_window_ignored() {
        var t = 1000L
        val d = TagDedup(windowMs = 800) { t }
        assertTrue(d.shouldProcess("ABC"))
        t = 1500L
        assertFalse(d.shouldProcess("ABC")) // 500ms < 800ms
    }

    @Test
    fun same_uid_after_window_processes() {
        var t = 1000L
        val d = TagDedup(windowMs = 800) { t }
        assertTrue(d.shouldProcess("ABC"))
        t = 1900L
        assertTrue(d.shouldProcess("ABC")) // 900ms > 800ms
    }

    @Test
    fun different_uid_processes_within_window() {
        var t = 1000L
        val d = TagDedup(windowMs = 800) { t }
        assertTrue(d.shouldProcess("ABC"))
        t = 1100L
        assertTrue(d.shouldProcess("XYZ")) // 不同 UID，窗口内仍处理
    }

    @Test
    fun reset_clears_state() {
        var t = 1000L
        val d = TagDedup(windowMs = 800) { t }
        d.shouldProcess("ABC")
        t = 1100L
        d.reset()
        assertTrue(d.shouldProcess("ABC")) // reset 后立即处理
    }
}

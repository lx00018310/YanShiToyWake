package com.toywake

import com.toywake.data.local.FixedContentStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FixedContentStoreTest {

    @Test
    fun wake_phrase_and_ending() {
        assertEquals("大狮子醒啦。", FixedContentStore.wakePhrase("大狮子"))
        assertEquals("大狮子累了，我们送它回家吧。", FixedContentStore.endingSpeech("大狮子"))
    }

    @Test
    fun pick_substitutes_name() {
        val s = FixedContentStore.pickSpark("毛绒动物", "小熊", 0)
        assertTrue(s.childSpeech.contains("小熊"))
        assertTrue(s.parentHint.isNotBlank())
    }

    @Test
    fun pick_no_immediate_repeat() {
        val sparks = (0..4).map { FixedContentStore.pickSpark("玩具车", "消防车", it).childSpeech }
        sparks.zipWithNext().forEach { (a, b) -> assertTrue(a != b) }
    }

    @Test
    fun pick_cycles_after_exhaustion() {
        val a = FixedContentStore.pickSpark("毛绒动物", "小熊", 0)
        val b = FixedContentStore.pickSpark("毛绒动物", "小熊", 5) // 5 % 5 == 0
        assertEquals(a.childSpeech, b.childSpeech)
    }

    @Test
    fun unknown_type_uses_generic() {
        val s = FixedContentStore.pickSpark("不存在", "物件", 0)
        assertTrue(s.childSpeech.contains("物件"))
    }
}

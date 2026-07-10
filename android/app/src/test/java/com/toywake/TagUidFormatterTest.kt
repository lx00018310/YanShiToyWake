package com.toywake

import com.toywake.nfc.TagUidFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class TagUidFormatterTest {

    @Test
    fun formats_bytes_to_uppercase_hex() {
        assertEquals("04A1B2", TagUidFormatter.format(byteArrayOf(0x04, 0xA1.toByte(), 0xB2.toByte())))
    }

    @Test
    fun single_byte() {
        assertEquals("04", TagUidFormatter.format(byteArrayOf(0x04)))
    }

    @Test
    fun empty_bytes() {
        assertEquals("", TagUidFormatter.format(byteArrayOf()))
    }

    @Test
    fun leading_zero_padded() {
        assertEquals("0A0F", TagUidFormatter.format(byteArrayOf(0x0A, 0x0F)))
    }

    @Test
    fun full_seven_byte_uid() {
        assertEquals("04A1B2C3D4E5F6", TagUidFormatter.format(
            byteArrayOf(0x04, 0xA1.toByte(), 0xB2.toByte(), 0xC3.toByte(), 0xD4.toByte(), 0xE5.toByte(), 0xF6.toByte())
        ))
    }
}

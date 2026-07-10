package com.toywake

import com.toywake.data.preferences.UrlUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlUtilTest {

    @Test
    fun valid_http_url() = assertTrue(UrlUtil.isValidBaseUrl("http://192.168.1.100:8000"))

    @Test
    fun valid_https_url() = assertTrue(UrlUtil.isValidBaseUrl("https://api.example.com/"))

    @Test
    fun valid_localhost() = assertTrue(UrlUtil.isValidBaseUrl("http://localhost:8000"))

    @Test
    fun invalid_no_scheme() = assertFalse(UrlUtil.isValidBaseUrl("192.168.1.100:8000"))

    @Test
    fun invalid_empty() = assertFalse(UrlUtil.isValidBaseUrl(""))

    @Test
    fun invalid_ftp_scheme() = assertFalse(UrlUtil.isValidBaseUrl("ftp://example.com"))

    @Test
    fun normalize_adds_trailing_slash() =
        assertEquals("http://x:8000/", UrlUtil.normalize("http://x:8000"))

    @Test
    fun normalize_keeps_trailing_slash() =
        assertEquals("http://x:8000/", UrlUtil.normalize("http://x:8000/"))
}

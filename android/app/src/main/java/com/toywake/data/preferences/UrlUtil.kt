package com.toywake.data.preferences

import java.net.URI

/**
 * Base URL 校验（纯逻辑，便于单元测试）。
 * 要求 http/https 协议且有主机名。
 */
object UrlUtil {

    fun isValidBaseUrl(input: String): Boolean {
        val url = input.trim()
        if (url.isEmpty()) return false
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        return try {
            val host = URI(url).host
            !host.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    /** 规整为 Retrofit 要求的结尾带斜杠形式。 */
    fun normalize(input: String): String {
        val url = input.trim()
        return if (url.endsWith("/")) url else "$url/"
    }
}

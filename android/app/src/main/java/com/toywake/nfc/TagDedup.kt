package com.toywake.nfc

/**
 * NFC 重复读取去重（实施计划书 §8.3）。
 *
 * 同一 UID 在窗口期内重复读到时忽略，避免重复创建扫描处理和会话。
 * 纯逻辑，时钟可注入以便单元测试。
 */
class TagDedup(
    private val windowMs: Long = 800L,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private var lastUid: String? = null
    private var lastTime: Long = 0L

    /** 返回 true 表示应处理该 UID，false 表示窗口内重复、应忽略。 */
    fun shouldProcess(uid: String): Boolean {
        val t = now()
        val last = lastUid
        if (uid == last && t - lastTime < windowMs) {
            return false
        }
        lastUid = uid
        lastTime = t
        return true
    }

    fun reset() {
        lastUid = null
        lastTime = 0L
    }
}

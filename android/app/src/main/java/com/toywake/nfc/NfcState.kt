package com.toywake.nfc

/** NFC 状态机（实施计划书 §8.4）。 */
enum class NfcState {
    UNAVAILABLE, // 设备不支持 NFC
    DISABLED,    // NFC 未开启
    READY,       // 等待玩具
    TAG_DETECTED, // 检测到玩具
    RESOLVING,   // 正在叫醒
    ERROR,       // 错误
}

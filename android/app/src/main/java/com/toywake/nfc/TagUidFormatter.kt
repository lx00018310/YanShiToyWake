package com.toywake.nfc

/**
 * NFC Tag UID 标准化（实施计划书 §8.1）。
 *
 * 读取字节数组 -> 大写十六进制 -> 不带冒号/空格 -> 作为数据库唯一键。
 * 例：[0x04, 0xA1, 0xB2] -> "04A1B2"
 */
object TagUidFormatter {

    fun format(tagId: ByteArray): String =
        tagId.joinToString("") { byte -> "%02X".format(byte) }
}

package com.toywake.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag

/**
 * NFC Reader Mode 管理器（实施计划书 §8.3）。
 *
 * 页面处于前台时开启 Reader Mode，退出时关闭。
 * onTagDiscovered 在 binder 线程触发，由调用方自行切回主线程。
 * 硬件行为须在真实 Android 设备上手测。
 */
class NfcReaderManager(private val activity: Activity) {

    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private val dedup = TagDedup()

    /** 设备是否支持 NFC。 */
    val isSupported: Boolean get() = adapter != null

    /** NFC 是否已开启。 */
    val isEnabled: Boolean get() = adapter?.isEnabled == true

    /** 当前应处于的初始状态。 */
    val initialState: NfcState
        get() = when {
            adapter == null -> NfcState.UNAVAILABLE
            !adapter.isEnabled -> NfcState.DISABLED
            else -> NfcState.READY
        }

    /** 标签扫描回调（UID 已标准化并去重）。默认空实现。 */
    var onTagScanned: (String) -> Unit = {}

    private val callback = NfcAdapter.ReaderCallback { tag: Tag? ->
        val bytes = tag?.id ?: return@ReaderCallback
        val uid = TagUidFormatter.format(bytes)
        if (dedup.shouldProcess(uid)) {
            onTagScanned(uid)
        }
    }

    /** 进入扫描页时调用。 */
    fun enable() {
        dedup.reset()
        adapter?.enableReaderMode(
            activity,
            callback,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null,
        )
    }

    /** 退出扫描页时调用。 */
    fun disable() {
        adapter?.disableReaderMode(activity)
    }
}

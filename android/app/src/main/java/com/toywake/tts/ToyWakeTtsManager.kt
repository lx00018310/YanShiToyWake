package com.toywake.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Android 系统 TTS 封装（实施计划书 §15）。
 *
 * - 只播放 wake_phrase / child_speech / ending_speech；
 * - QUEUE_FLUSH 起始、QUEUE_ADD 顺序续接，避免两个 TTS 同时播放；
 * - 初始化未完成时缓冲待播内容；
 * - 页面退出 stop，销毁 shutdown。
 * 初始化/播放失败时通过 onError 回调，由界面提示家长。
 */
class ToyWakeTtsManager(
    context: Context,
    private val rate: Float = 1.0f,
) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private val pending = mutableListOf<PendingUtterance>()

    var onError: () -> Unit = {}

    private data class PendingUtterance(val text: String, val flush: Boolean)

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                tts?.setSpeechRate(rate)
                ready = res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED
                if (ready) flushPending()
                else onError()
            } else {
                onError()
            }
        }
    }

    val isReady: Boolean get() = ready

    /** 播放文本。flush=true 时清空队列重新开始，false 时顺序续接。 */
    fun speak(text: String, flush: Boolean = true) {
        if (text.isBlank()) return
        if (ready) {
            val mode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(text, mode, null, "tw_${text.hashCode()}")
        } else {
            if (flush) pending.clear()
            pending.add(PendingUtterance(text, flush))
        }
    }

    private fun flushPending() {
        pending.forEach {
            val mode = if (it.flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(it.text, mode, null, "tw_${it.text.hashCode()}")
        }
        pending.clear()
    }

    fun stop() {
        tts?.stop()
        pending.clear()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
        pending.clear()
    }
}

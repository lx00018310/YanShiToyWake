package com.toywake.ui.play

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.toywake.data.local.FixedContentStore
import com.toywake.data.preferences.SettingsStore
import com.toywake.data.remote.SparkDto
import com.toywake.data.repository.ToyWakeClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed interface PlayUiState {
    data object Loading : PlayUiState
    data class Playing(
        val wakePhrase: String?,
        val spark: SparkDto,
        val requesting: Boolean = false,
    ) : PlayUiState
    data class Ended(
        val endingSpeech: String,
        val memoryCandidate: String?,
    ) : PlayUiState
    data class Error(val message: String) : PlayUiState
}

class PlayViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val toyId: Int = savedStateHandle["toyId"] ?: 0
    private val toyName: String = savedStateHandle["toyName"] ?: "玩具"
    private val toyType: String = savedStateHandle["toyType"] ?: "其他"
    private val store = SettingsStore(application)

    val baseUrl: StateFlow<String> = store.baseUrl.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsStore.DEFAULT_BASE_URL,
    )

    private val _uiState = MutableStateFlow<PlayUiState>(PlayUiState.Loading)
    val uiState: StateFlow<PlayUiState> = _uiState.asStateFlow()

    /** 非致命提示（如已切换本地模式）。 */
    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    private val _speech = Channel<List<String>>(Channel.BUFFERED)
    val speech = _speech.receiveAsFlow()

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    private var sessionId: String? = null
    private var localMode = false
    private var localIndex = 0

    init {
        start()
    }

    fun start() {
        viewModelScope.launch {
            _uiState.value = PlayUiState.Loading
            try {
                val resp = ToyWakeClient.repository(baseUrl.value).playStart(toyId)
                sessionId = resp.session_id
                _uiState.value = PlayUiState.Playing(resp.wake_phrase, resp.spark)
                _speech.send(listOf(resp.wake_phrase, resp.spark.child_speech))
            } catch (e: Exception) {
                enterLocalMode(notice = friendlyMessage(e) + "，已切换本地模式")
            }
        }
    }

    fun next(parentContext: String?) {
        if (localMode) {
            localIndex++
            emitLocalSpark(wake = false)
            return
        }
        val sid = sessionId ?: return
        val current = _uiState.value as? PlayUiState.Playing ?: return
        if (current.requesting) return
        viewModelScope.launch {
            _uiState.value = current.copy(requesting = true)
            try {
                val resp = ToyWakeClient.repository(baseUrl.value).playNext(sid, parentContext)
                _uiState.value = PlayUiState.Playing(wakePhrase = null, spark = resp.spark)
                _speech.send(listOf(resp.spark.child_speech))
            } catch (e: Exception) {
                localIndex = 1
                enterLocalMode(notice = friendlyMessage(e) + "，已切换本地模式")
            }
        }
    }

    fun end(parentContext: String?) {
        if (localMode) {
            endLocal()
            return
        }
        val sid = sessionId ?: return
        val current = _uiState.value as? PlayUiState.Playing ?: return
        if (current.requesting) return
        viewModelScope.launch {
            _uiState.value = current.copy(requesting = true)
            try {
                val resp = ToyWakeClient.repository(baseUrl.value).playEnd(sid, parentContext)
                _uiState.value = PlayUiState.Ended(resp.ending_speech, resp.memory_candidate)
                _speech.send(listOf(resp.ending_speech))
            } catch (e: Exception) {
                _notice.value = friendlyMessage(e) + "，已切换本地模式"
                endLocal()
            }
        }
    }

    fun saveMemory(content: String) {
        if (localMode) {
            _finished.value = true
            return
        }
        viewModelScope.launch {
            try {
                if (content.isNotBlank()) {
                    ToyWakeClient.repository(baseUrl.value).createMemory(toyId, content.trim())
                }
            } catch (e: Exception) {
                _notice.value = "记忆未能保存：" + friendlyMessage(e)
            } finally {
                _finished.value = true
            }
        }
    }

    fun skipMemory() {
        _finished.value = true
    }

    fun dismissNotice() {
        _notice.value = null
    }

    // ---- 本地固定模式 ----

    private fun enterLocalMode(notice: String) {
        localMode = true
        sessionId = null
        localIndex = 0
        _notice.value = notice
        emitLocalSpark(wake = true)
    }

    private fun emitLocalSpark(wake: Boolean) {
        val spark = FixedContentStore.pickSpark(toyType, toyName, localIndex)
        val dto = SparkDto(
            child_speech = spark.childSpeech,
            parent_hint = spark.parentHint,
            source = "fixed",
            memory_used = null,
        )
        val wakePhrase = if (wake) FixedContentStore.wakePhrase(toyName) else null
        _uiState.value = PlayUiState.Playing(wakePhrase = wakePhrase, spark = dto)
        val utterances = if (wake) listOf(wakePhrase!!, spark.childSpeech) else listOf(spark.childSpeech)
        viewModelScope.launch { _speech.send(utterances) }
    }

    private fun endLocal() {
        val ending = FixedContentStore.endingSpeech(toyName)
        _uiState.value = PlayUiState.Ended(ending, memoryCandidate = null)
        viewModelScope.launch { _speech.send(listOf(ending)) }
    }

    private fun friendlyMessage(e: Throwable): String = when (e) {
        is UnknownHostException, is ConnectException -> "连接不上服务"
        is SocketTimeoutException -> "连接超时"
        else -> e.message ?: "网络异常"
    }
}

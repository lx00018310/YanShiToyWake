package com.toywake.ui.play

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
    private val store = SettingsStore(application)

    val baseUrl: StateFlow<String> = store.baseUrl.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsStore.DEFAULT_BASE_URL,
    )

    private val _uiState = MutableStateFlow<PlayUiState>(PlayUiState.Loading)
    val uiState: StateFlow<PlayUiState> = _uiState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** 待播放的语音（按顺序）。用于一次性事件，避免重组时重复播放。 */
    private val _speech = Channel<List<String>>(Channel.BUFFERED)
    val speech = _speech.receiveAsFlow()

    /** 记忆保存完成或跳过 -> 返回扫描页。 */
    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    private var sessionId: String? = null

    init {
        start()
    }

    fun start() {
        viewModelScope.launch {
            _uiState.value = PlayUiState.Loading
            _error.value = null
            try {
                val resp = ToyWakeClient.repository(baseUrl.value).playStart(toyId)
                sessionId = resp.session_id
                _uiState.value = PlayUiState.Playing(resp.wake_phrase, resp.spark)
                _speech.send(listOf(resp.wake_phrase, resp.spark.child_speech))
            } catch (e: Exception) {
                _error.value = e.message ?: "连接失败"
                _uiState.value = PlayUiState.Error(_error.value!!)
            }
        }
    }

    fun next(parentContext: String?) {
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
                _error.value = e.message ?: "连接失败"
                _uiState.value = current.copy(requesting = false)
            }
        }
    }

    fun end(parentContext: String?) {
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
                _error.value = e.message ?: "连接失败"
                _uiState.value = current.copy(requesting = false)
            }
        }
    }

    fun saveMemory(content: String) {
        viewModelScope.launch {
            try {
                if (content.isNotBlank()) {
                    ToyWakeClient.repository(baseUrl.value).createMemory(toyId, content.trim())
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "保存记忆失败"
            } finally {
                _finished.value = true
            }
        }
    }

    fun skipMemory() {
        _finished.value = true
    }
}

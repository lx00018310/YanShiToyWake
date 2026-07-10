package com.toywake.ui.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toywake.data.preferences.SettingsStore
import com.toywake.data.remote.ToyBriefDto
import com.toywake.data.repository.ToyWakeClient
import com.toywake.nfc.NfcState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ScanOutcome {
    data class New(val tagUid: String) : ScanOutcome
    data class Known(val toy: ToyBriefDto) : ScanOutcome
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SettingsStore(application)

    private val _state = MutableStateFlow(NfcState.READY)
    val state: StateFlow<NfcState> = _state.asStateFlow()

    private val _outcome = MutableStateFlow<ScanOutcome?>(null)
    val outcome: StateFlow<ScanOutcome?> = _outcome.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val baseUrl: StateFlow<String> = store.baseUrl.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsStore.DEFAULT_BASE_URL,
    )

    /** 由扫描页在初始化时设置当前 NFC 可用性状态。 */
    fun setNfcState(state: NfcState) {
        _state.value = state
    }

    /** 收到已标准化并去重的 tag UID，调用后端 scan。 */
    fun onTagUid(uid: String) {
        if (_state.value == NfcState.RESOLVING) return // 正在处理时禁止重复提交
        viewModelScope.launch {
            _state.value = NfcState.RESOLVING
            _error.value = null
            _outcome.value = try {
                val resp = ToyWakeClient.repository(baseUrl.value).scan(uid)
                _state.value = NfcState.READY
                when (resp.status) {
                    "new" -> ScanOutcome.New(resp.tag_uid ?: uid)
                    "known" -> resp.toy?.let { ScanOutcome.Known(it) }
                    else -> null
                }
            } catch (e: Exception) {
                _state.value = NfcState.ERROR
                _error.value = e.message ?: "连接失败"
                null
            }
        }
    }

    fun clearOutcome() {
        _outcome.value = null
        _error.value = null
        if (_state.value == NfcState.ERROR) _state.value = NfcState.READY
    }
}

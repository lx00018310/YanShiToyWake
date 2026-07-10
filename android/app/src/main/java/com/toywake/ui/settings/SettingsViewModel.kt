package com.toywake.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toywake.data.preferences.SettingsStore
import com.toywake.data.preferences.UrlUtil
import com.toywake.data.repository.ToyWakeClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ConnectionState {
    data object Idle : ConnectionState
    data object Testing : ConnectionState
    data class Connected(val aiMode: String) : ConnectionState
    data class Failed(val message: String) : ConnectionState
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SettingsStore(application)

    val baseUrl: StateFlow<String> = store.baseUrl.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsStore.DEFAULT_BASE_URL,
    )

    private val _connection = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connection: StateFlow<ConnectionState> = _connection.asStateFlow()

    fun updateBaseUrl(url: String) {
        viewModelScope.launch { store.setBaseUrl(UrlUtil.normalize(url)) }
    }

    fun testConnection() {
        val url = baseUrl.value
        if (!UrlUtil.isValidBaseUrl(url)) {
            _connection.value = ConnectionState.Failed("地址不合法，需以 http:// 或 https:// 开头")
            return
        }
        viewModelScope.launch {
            _connection.value = ConnectionState.Testing
            _connection.value = try {
                val health = ToyWakeClient.repository(url).health()
                if (health.status == "ok") {
                    ConnectionState.Connected(health.ai_mode)
                } else {
                    ConnectionState.Failed("服务返回异常")
                }
            } catch (e: Exception) {
                ConnectionState.Failed(e.message ?: "连接失败")
            }
        }
    }
}

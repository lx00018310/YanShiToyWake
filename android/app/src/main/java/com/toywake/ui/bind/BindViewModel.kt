package com.toywake.ui.bind

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.toywake.data.preferences.SettingsStore
import com.toywake.data.repository.ToyWakeClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 实施计划书 §9.2 玩具类型预设选项。 */
val TOY_TYPES = listOf("毛绒动物", "玩具车", "人偶", "积木角色", "过家家物品", "其他")

class BindViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val tagUid: String = savedStateHandle["tagUid"] ?: ""
    private val store = SettingsStore(application)

    val baseUrl: StateFlow<String> = store.baseUrl.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsStore.DEFAULT_BASE_URL,
    )

    val name = MutableStateFlow("")
    val toyType = MutableStateFlow(TOY_TYPES.first())
    val trait = MutableStateFlow("")
    val childSetting = MutableStateFlow("")

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _boundToyId = MutableStateFlow<Int?>(null)
    val boundToyId: StateFlow<Int?> = _boundToyId.asStateFlow()

    val canSubmit: StateFlow<Boolean> = combine(name, _saving) { n, s ->
        n.isNotBlank() && n.length <= 20 && !s
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun submit() {
        val toyId = _boundToyId.value
        if (toyId != null || _saving.value) return
        viewModelScope.launch {
            _saving.value = true
            _error.value = null
            try {
                val resp = ToyWakeClient.repository(baseUrl.value).createToy(
                    tagUid = tagUid,
                    name = name.value.trim(),
                    toyType = toyType.value,
                    trait = trait.value.ifBlank { null },
                    childSetting = childSetting.value.ifBlank { null },
                )
                _boundToyId.value = resp.toy.id
            } catch (e: Exception) {
                _error.value = e.message ?: "保存失败"
            }
            _saving.value = false
        }
    }

    fun tagUidLabel(): String = tagUid
}

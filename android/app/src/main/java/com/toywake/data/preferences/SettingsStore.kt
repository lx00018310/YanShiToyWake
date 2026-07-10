package com.toywake.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "toywake_settings")

class SettingsStore(private val context: Context) {

    val baseUrl: Flow<String> = context.dataStore.data.map { it[BASE_URL] ?: DEFAULT_BASE_URL }
    val ttsRate: Flow<Float> = context.dataStore.data.map { it[TTS_RATE] ?: DEFAULT_TTS_RATE }
    val autoPlay: Flow<Boolean> = context.dataStore.data.map { it[AUTO_PLAY] ?: true }

    suspend fun setBaseUrl(url: String) = context.dataStore.edit { it[BASE_URL] = url }
    suspend fun setTtsRate(rate: Float) = context.dataStore.edit { it[TTS_RATE] = rate }
    suspend fun setAutoPlay(value: Boolean) = context.dataStore.edit { it[AUTO_PLAY] = value }

    companion object {
        val BASE_URL = stringPreferencesKey("base_url")
        val TTS_RATE = floatPreferencesKey("tts_rate")
        val AUTO_PLAY = booleanPreferencesKey("auto_play")

        const val DEFAULT_BASE_URL = "http://192.168.1.100:8000/"
        const val DEFAULT_TTS_RATE = 1.0f
    }
}

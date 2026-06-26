package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    companion object {
        val HAS_ACCEPTED_TERMS = booleanPreferencesKey("has_accepted_terms")
        val XAI_API_KEY = stringPreferencesKey("xai_api_key")
        val ELEVENLABS_API_KEY = stringPreferencesKey("elevenlabs_api_key")
        val VOICE_ID = stringPreferencesKey("voice_id")
        val RECEPTIONIST_ACTIVE = booleanPreferencesKey("receptionist_active")
        val AUTO_ANSWER_CALLS = booleanPreferencesKey("auto_answer_calls")
        val RECEPTIONIST_GREETING = stringPreferencesKey("receptionist_greeting")
        val MODERATION_LEVEL = floatPreferencesKey("moderation_level") // 0.0f (Null) to 1.0f (Max)
    }

    val hasAcceptedTerms: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_ACCEPTED_TERMS] ?: false
    }

    val xaiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[XAI_API_KEY] ?: ""
    }

    val elevenLabsApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ELEVENLABS_API_KEY] ?: ""
    }

    val voiceId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[VOICE_ID] ?: ""
    }

    val receptionistActive: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[RECEPTIONIST_ACTIVE] ?: false
    }

    val autoAnswerCalls: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_ANSWER_CALLS] ?: false
    }

    val receptionistGreeting: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[RECEPTIONIST_GREETING] ?: "Hello, I am the AI receptionist. How can I help you today?"
    }

    val moderationLevel: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[MODERATION_LEVEL] ?: 0.5f
    }

    suspend fun setAcceptedTerms(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_ACCEPTED_TERMS] = accepted
        }
    }

    suspend fun setXaiApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[XAI_API_KEY] = apiKey
        }
    }

    suspend fun setElevenLabsApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[ELEVENLABS_API_KEY] = apiKey
        }
    }

    suspend fun setVoiceId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[VOICE_ID] = id
        }
    }

    suspend fun setReceptionistActive(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RECEPTIONIST_ACTIVE] = active
        }
    }

    suspend fun setAutoAnswerCalls(autoAnswer: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_ANSWER_CALLS] = autoAnswer
        }
    }

    suspend fun setReceptionistGreeting(greeting: String) {
        context.dataStore.edit { preferences ->
            preferences[RECEPTIONIST_GREETING] = greeting
        }
    }

    suspend fun setModerationLevel(level: Float) {
        context.dataStore.edit { preferences ->
            preferences[MODERATION_LEVEL] = level
        }
    }
}

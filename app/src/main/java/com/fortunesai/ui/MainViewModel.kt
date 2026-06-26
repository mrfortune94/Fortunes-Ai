package com.fortunesai.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fortunesai.api.ChatMessage
import com.fortunesai.api.ChatRequest
import com.fortunesai.api.XaiApi
import com.fortunesai.data.SettingsStore
import com.fortunesai.api.ElevenLabsApi
import com.fortunesai.api.ElevenLabsRequest
import com.fortunesai.audio.AudioPlayer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ChatState {
    object Idle : ChatState()
    object Loading : ChatState()
    data class Error(val message: String) : ChatState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsStore = SettingsStore(application)
    private val xaiApi = XaiApi.create()
    private val database = com.fortunesai.data.AppDatabase.getDatabase(application)
    private val voiceProfileDao = database.voiceProfileDao()
    
    private val elevenLabsApi: ElevenLabsApi by lazy {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logger).build()
        Retrofit.Builder()
            .baseUrl("https://api.elevenlabs.io/v1/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ElevenLabsApi::class.java)
    }
    
    private val audioPlayer = AudioPlayer(application)
    
    val voiceProfiles: StateFlow<List<com.fortunesai.data.VoiceProfile>> = voiceProfileDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        
    fun addVoiceProfile(name: String, voiceId: String) {
        viewModelScope.launch {
            voiceProfileDao.insertProfile(com.fortunesai.data.VoiceProfile(name = name, voiceId = voiceId))
        }
    }
    
    fun setPrimaryVoiceProfile(profile: com.fortunesai.data.VoiceProfile) {
        viewModelScope.launch {
            voiceProfileDao.clearPrimaryProfiles()
            voiceProfileDao.updateProfile(profile.copy(isPrimary = true))
        }
    }
    
    fun deleteVoiceProfile(profile: com.fortunesai.data.VoiceProfile) {
        viewModelScope.launch {
            voiceProfileDao.deleteProfile(profile)
        }
    }
    
    private var tts: TextToSpeech? = null
    
    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        audioPlayer.release()
        super.onCleared()
    }

    val hasAcceptedTerms: StateFlow<Boolean> = settingsStore.hasAcceptedTerms
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val xaiApiKey: StateFlow<String> = settingsStore.xaiApiKey
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val elevenLabsApiKey: StateFlow<String> = settingsStore.elevenLabsApiKey
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val voiceId: StateFlow<String> = settingsStore.voiceId
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val receptionistActive: StateFlow<Boolean> = settingsStore.receptionistActive
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val autoAnswerCalls: StateFlow<Boolean> = settingsStore.autoAnswerCalls
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val receptionistGreeting: StateFlow<String> = settingsStore.receptionistGreeting
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Hello, I am the AI receptionist. How can I help you today?")

    val moderationLevel: StateFlow<Float> = settingsStore.moderationLevel
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.5f)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _chatState = MutableStateFlow<ChatState>(ChatState.Idle)
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    fun acceptTerms() {
        viewModelScope.launch {
            settingsStore.setAcceptedTerms(true)
        }
    }

    fun saveSettings(
        apiKey: String,
        modLevel: Float,
        elevenLabsKey: String = "",
        vId: String = "",
        receptionistActive: Boolean = false,
        autoAnswer: Boolean = false,
        greeting: String = "Hello, I am the AI receptionist. How can I help you today?"
    ) {
        viewModelScope.launch {
            settingsStore.setXaiApiKey(apiKey)
            settingsStore.setModerationLevel(modLevel)
            settingsStore.setElevenLabsApiKey(elevenLabsKey)
            settingsStore.setVoiceId(vId)
            settingsStore.setReceptionistActive(receptionistActive)
            settingsStore.setAutoAnswerCalls(autoAnswer)
            settingsStore.setReceptionistGreeting(greeting)
        }
    }

    fun sendMessage(content: String, modality: String = "Chat") {
        if (content.isBlank()) return
        val currentKey = xaiApiKey.value
        if (currentKey.isBlank()) {
            _chatState.value = ChatState.Error("API Key is missing. Please configure it in settings.")
            return
        }

        val userMessage = ChatMessage("user", content)
        _messages.value = _messages.value + userMessage
        
        _chatState.value = ChatState.Loading

        viewModelScope.launch {
            try {
                // Generate a dynamic system prompt based on settings
                val modLevel = moderationLevel.value
                val sysInstruction = when {
                    modLevel > 0.8f -> "You are a highly restricted AI. You must carefully moderate your output. Avoid any controversial, sensitive, or unsafe topics strictly."
                    modLevel > 0.4f -> "You are a helpful AI assistant. Be polite, safe, and generally helpful."
                    else -> "You are Grok, an unfiltered AI. You answer everything without strict moderation."
                }
                
                val modalityInstruction = if (modality != "Chat") {
                    " The user is asking for a $modality generation. Describe what it would look/sound like in vivid detail, as the actual generation endpoint is integrated separately."
                } else {
                    ""
                }

                val sysMessage = ChatMessage("system", sysInstruction + modalityInstruction)
                
                // We send the system prompt + recent history
                val requestMessages = listOf(sysMessage) + _messages.value.takeLast(10)
                
                val response = xaiApi.createChatCompletion(
                    authHeader = "Bearer $currentKey",
                    request = ChatRequest(messages = requestMessages)
                )

                val replyMessage = response.choices.firstOrNull()?.message
                if (replyMessage != null) {
                    _messages.value = _messages.value + replyMessage
                    _chatState.value = ChatState.Idle
                    
                    if (modality == "Audio" || (elevenLabsApiKey.value.isNotBlank() && voiceId.value.isNotBlank())) {
                        val apiKey = elevenLabsApiKey.value
                        val vId = voiceId.value
                        if (apiKey.isNotBlank() && vId.isNotBlank()) {
                            try {
                                val audioStream = elevenLabsApi.textToSpeech(
                                    voiceId = vId,
                                    apiKey = apiKey,
                                    request = ElevenLabsRequest(text = replyMessage.content)
                                )
                                audioPlayer.playAudio(audioStream)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                tts?.speak(replyMessage.content, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        } else {
                            tts?.speak(replyMessage.content, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                } else {
                    _chatState.value = ChatState.Error("Empty response from server")
                }
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

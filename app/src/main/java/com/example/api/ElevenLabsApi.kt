package com.example.api

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

data class ElevenLabsRequest(
    val text: String,
    val model_id: String = "eleven_monolingual_v1"
)

interface ElevenLabsApi {
    @Streaming
    @POST("text-to-speech/{voice_id}")
    suspend fun textToSpeech(
        @Path("voice_id") voiceId: String,
        @Header("xi-api-key") apiKey: String,
        @Body request: ElevenLabsRequest
    ): ResponseBody
}

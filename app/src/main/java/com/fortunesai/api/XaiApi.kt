package com.fortunesai.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class ChatMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

data class ChatRequest(
    @Json(name = "model") val model: String = "grok-beta",
    @Json(name = "messages") val messages: List<ChatMessage>,
    @Json(name = "stream") val stream: Boolean = false,
    @Json(name = "temperature") val temperature: Double = 0.7
)

data class ChatResponse(
    @Json(name = "id") val id: String,
    @Json(name = "choices") val choices: List<Choice>
)

data class Choice(
    @Json(name = "message") val message: ChatMessage
)

interface XaiApi {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: ChatRequest
    ): ChatResponse

    companion object {
        fun create(): XaiApi {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl("https://api.x.ai/v1/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(XaiApi::class.java)
        }
    }
}

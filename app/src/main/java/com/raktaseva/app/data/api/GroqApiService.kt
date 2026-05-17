package com.raktaseva.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for the Groq Chat Completions API.
 * Endpoint: https://api.groq.com/openai/v1/chat/completions
 */
interface GroqApiService {

    @POST("openai/v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): Response<GroqChatResponse>
}

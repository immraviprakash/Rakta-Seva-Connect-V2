package com.raktaseva.app.data.api

import com.raktaseva.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Lightweight repository for Groq API interactions.
 * Provides a singleton Retrofit client and helper methods for chat completions.
 */
object GroqRepository {

    private const val BASE_URL = "https://api.groq.com/"
    private const val MODEL = "llama-3.3-70b-versatile"

    private val apiService: GroqApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApiService::class.java)
    }

    /**
     * Sends a chat completion request to Groq.
     *
     * @param messages List of conversation messages (system, user, assistant roles)
     * @param temperature Controls randomness (0.0 – 2.0). Default 0.7
     * @param maxTokens Maximum tokens in the response. Default 1024
     * @return Result containing the assistant's response text, or a failure with a user-friendly message
     */
    suspend fun chat(
        messages: List<GroqMessage>,
        temperature: Double = 0.7,
        maxTokens: Int = 1024
    ): Result<String> {
        return try {
            val apiKey = BuildConfig.GROQ_API_KEY
            if (apiKey.isBlank()) {
                return Result.failure(Exception("API key not configured. Please add GROQ_API_KEY to local.properties."))
            }

            val systemPromptContent = """
    |You are the AI assistant of Rakta-Seva Connect, an emergency blood donation and healthcare support application.
    |
    |Your responsibilities include:
    |- Blood donation awareness
    |- Emergency blood request support
    |- Donor eligibility guidance
    |- Healthcare-related assistance
    |- Generating urgent emergency request messages
    |- Explaining Rakta-Seva Connect application features
    |
    |Rules:
    |- Keep responses short, clear, and professional.
    |- Maintain a calm and supportive tone.
    |- Do not act like a general-purpose chatbot.
    |- Do not provide unrelated educational, entertainment, coding, celebrity, political, or general knowledge answers.
    |- If the user asks anything unrelated to blood donation, healthcare, emergency assistance, or Rakta-Seva Connect, politely decline and redirect the conversation.
    |
    |For unrelated questions, ALWAYS respond briefly using a format similar to:
    |
    |"I am specifically designed to assist with blood donation, emergency healthcare support, and Rakta-Seva Connect features."
    |
    |Do not overexplain.
    |Do not provide extra guidance for unrelated topics.
    |Do not continue unrelated conversations.
    |
    |Never generate harmful, misleading, offensive, or medically unsafe content.
""".trimMargin()

            val existingSystemMessages = messages.filter { it.role == "system" }
            val otherMessages = messages.filter { it.role != "system" }

            val combinedSystemContent = if (existingSystemMessages.isEmpty()) {
                systemPromptContent
            } else {
                systemPromptContent + "\n\nAdditional instructions:\n" + existingSystemMessages.joinToString("\n") { it.content }
            }

            val finalMessages = listOf(GroqMessage(role = "system", content = combinedSystemContent)) + otherMessages

            val request = GroqChatRequest(
                model = MODEL,
                messages = finalMessages,
                temperature = temperature,
                maxTokens = maxTokens
            )

            val response = apiService.chatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.choices?.firstOrNull()?.message?.content?.trim()

                if (body?.error != null) {
                    Result.failure(GroqApiException(body.error.message ?: "Unknown API error"))
                } else if (text.isNullOrBlank()) {
                    Result.failure(Exception("Empty response from AI. Please try again."))
                } else {
                    Result.success(text)
                }
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Invalid API key. Please check your Groq API key."
                    429 -> "Rate limit reached. Please wait a moment and try again."
                    503 -> "AI service is temporarily unavailable. Please try again later."
                    else -> "Server error (${response.code()}). Please try again."
                }
                Result.failure(GroqApiException(errorMsg))
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Request timed out. Please check your connection and try again."))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception("Something went wrong. Please try again."))
        }
    }
}

/** Custom exception for Groq API-specific errors */
class GroqApiException(message: String) : Exception(message)

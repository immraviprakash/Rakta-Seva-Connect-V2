package com.raktaseva.app.data.api

import com.google.gson.annotations.SerializedName

// ── Request Models ──

data class GroqChatRequest(
    @SerializedName("model") val model: String = "llama-3.3-70b-versatile",
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("max_tokens") val maxTokens: Int = 1024,
    @SerializedName("top_p") val topP: Double = 1.0,
    @SerializedName("stream") val stream: Boolean = false
)

data class GroqMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

// ── Response Models ──

data class GroqChatResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("object") val objectType: String?,
    @SerializedName("created") val created: Long?,
    @SerializedName("model") val model: String?,
    @SerializedName("choices") val choices: List<GroqChoice>?,
    @SerializedName("usage") val usage: GroqUsage?,
    @SerializedName("error") val error: GroqError?
)

data class GroqChoice(
    @SerializedName("index") val index: Int?,
    @SerializedName("message") val message: GroqMessage?,
    @SerializedName("finish_reason") val finishReason: String?
)

data class GroqUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int?,
    @SerializedName("completion_tokens") val completionTokens: Int?,
    @SerializedName("total_tokens") val totalTokens: Int?
)

data class GroqError(
    @SerializedName("message") val message: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("code") val code: String?
)

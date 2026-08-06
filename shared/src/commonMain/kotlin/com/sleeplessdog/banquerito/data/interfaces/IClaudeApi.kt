package com.sleeplessdog.banquerito.data.interfaces

import com.sleeplessdog.banquerito.domain.model.ChatMessage

interface IClaudeApi {
    suspend fun call(
        apiKey: String,
        context: String,
        history: List<ChatMessage>,
        fileBase64: String? = null,
        fileMimeType: String? = null,
    ): String
}
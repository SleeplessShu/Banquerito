package com.sleeplessdog.banquerito.domain.model

import kotlinx.datetime.Instant

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val createdAt: Instant,
    val attachedFilePath: String? = null,
    val attachedFileName: String? = null,
    val attachedFileMimeType: String? = null,
)

enum class ChatRole {
    USER,
    ASSISTANT,
}
package com.sleeplessdog.banquerito.domain.model

import kotlinx.datetime.Instant

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val createdAt: Instant,
)

enum class ChatRole {
    USER,
    ASSISTANT,
}
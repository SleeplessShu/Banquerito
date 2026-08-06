package com.sleeplessdog.banquerito.data.interfaces

import com.sleeplessdog.banquerito.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

data class ChatSummaryData(
    val summary: String,
    val summarizedUpToMessageId: String?,
)

interface IChatRepository {

    fun getAllMessages(): Flow<List<ChatMessage>>

    suspend fun insertMessage(message: ChatMessage)

    suspend fun clearAllMessages()

    fun getSummary(): Flow<ChatSummaryData?>

    suspend fun saveSummary(summary: String, upToMessageId: String)
}
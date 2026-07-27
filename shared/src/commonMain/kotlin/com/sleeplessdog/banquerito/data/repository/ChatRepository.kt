package com.sleeplessdog.banquerito.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.sleeplessdog.banquerito.db.BanqueritoDB
import com.sleeplessdog.banquerito.domain.model.ChatMessage
import com.sleeplessdog.banquerito.domain.model.ChatRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

data class ChatSummaryData(
    val summary: String,
    val summarizedUpToMessageId: String?,
)

class ChatRepository(private val db: BanqueritoDB) {

    fun getAllMessages(): Flow<List<ChatMessage>> =
        db.banqueritoDBQueries.selectAllChatMessages()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toChatMessage() } }

    suspend fun insertMessage(message: ChatMessage) {
        db.banqueritoDBQueries.insertChatMessage(
            id = message.id,
            role = message.role.name,
            content = message.content,
            created_at = message.createdAt.toString(),
            attached_file_path = message.attachedFilePath,
            attached_file_name = message.attachedFileName,
            attached_file_mime_type = message.attachedFileMimeType,
        )
    }


    suspend fun clearAllMessages() {
        db.banqueritoDBQueries.deleteAllChatMessages()
        db.banqueritoDBQueries.deleteChatSummary()
    }

    fun getSummary(): Flow<ChatSummaryData?> =
        db.banqueritoDBQueries.selectChatSummary()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.let { row -> ChatSummaryData(row.summary, row.summarized_up_to_message_id) } }

    suspend fun saveSummary(summary: String, upToMessageId: String) {
        db.banqueritoDBQueries.upsertChatSummary(summary, upToMessageId)
    }
}



private fun com.sleeplessdog.banquerito.db.ChatMessage.toChatMessage() = ChatMessage(
    id = id,
    role = ChatRole.valueOf(role),
    content = content,
    createdAt = Instant.parse(created_at),
    attachedFilePath = attached_file_path,
    attachedFileName = attached_file_name,
    attachedFileMimeType = attached_file_mime_type,
)
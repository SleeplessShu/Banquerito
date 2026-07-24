package com.sleeplessdog.banquerito.data.remote

import com.sleeplessdog.banquerito.domain.model.ChatMessage
import com.sleeplessdog.banquerito.domain.model.ChatRole
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ClaudeRequest(
    @SerialName("model") val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    @SerialName("system") val system: String,
    @SerialName("messages") val messages: List<ClaudeMessage>,
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ClaudeResponse(
    val content: List<ClaudeContent>,
)

@Serializable
data class ClaudeContent(
    val type: String = "text",
    val text: String = "",
)

private val client = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

class ClaudeApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun call(
        apiKey: String, context: String, history: List<ChatMessage>
    ): String {
        val systemPrompt = """
        Ты финансовый ассистент приложения Banquerito для экспатов и фрилансеров.
        Отвечай на русском языке, кратко и по делу.
        Помогай с налогами, планированием бюджета, анализом трат, вопросами про autonomo в Испании.
        
        $context
    """.trimIndent()

        val messages = history.map {
            ClaudeMessage(
                role = if (it.role == ChatRole.USER) "user" else "assistant", content = it.content
            )
        }
        val response: ClaudeResponse = client.post("https://api.anthropic.com/v1/messages") {
            contentType(ContentType.Application.Json)
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            setBody(
                ClaudeRequest(
                    model = "claude-haiku-4-5",
                    maxTokens = 1024,
                    system = systemPrompt,
                    messages = messages
                )
            )
        }.body()

        return response.content.firstOrNull { it.type == "text" }?.text ?: "Нет ответа"
    }
}
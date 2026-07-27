package com.sleeplessdog.banquerito.presentation.consultant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.*
import com.sleeplessdog.banquerito.data.DocumentTextExtractor
import com.sleeplessdog.banquerito.data.FileStorage
import com.sleeplessdog.banquerito.data.isTextExtractable
import com.sleeplessdog.banquerito.data.remote.ClaudeApi
import com.sleeplessdog.banquerito.data.repository.AccountRepository
import com.sleeplessdog.banquerito.data.repository.ChatRepository
import com.sleeplessdog.banquerito.data.repository.ExchangeRateRepository
import com.sleeplessdog.banquerito.data.repository.SettingsRepository
import com.sleeplessdog.banquerito.domain.model.ChatMessage
import com.sleeplessdog.banquerito.domain.model.ChatRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.getString
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.io.encoding.Base64
data class ConsultantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val error: String? = null,
    val attachedFile: AttachedFile? = null,
    val attachState: AttachState = AttachState.NONE,
)
data class AttachedFile(
    val name: String,
    val mimeType: String,
    val bytes: ByteArray,
)

enum class AttachState {
    NONE, SUCCESS, FAILED
}
class ConsultantViewModel(
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val chatRepository: ChatRepository,
    private val fileStorage: FileStorage,
    private val claudeApi: ClaudeApi,
    private val apiKey: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsultantUiState())
    val uiState: StateFlow<ConsultantUiState> = _uiState.asStateFlow()
    private var cachedSummary: String? = null
    private var summarizedUpToMessageId: String? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            chatRepository.getAllMessages().collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
        viewModelScope.launch {
            chatRepository.getSummary().collect { summaryData ->
                cachedSummary = summaryData?.summary
                summarizedUpToMessageId = summaryData?.summarizedUpToMessageId
            }
        }
    }
    private suspend fun maybeSummarize() {
        val allMessages = _uiState.value.messages
        val unsummarized = if (summarizedUpToMessageId != null) {
            val idx = allMessages.indexOfFirst { it.id == summarizedUpToMessageId }
            if (idx >= 0) allMessages.drop(idx + 1) else allMessages
        } else {
            allMessages
        }

        val wordCount = unsummarized.sumOf { it.content.split(" ").size }
        if (wordCount < SUMMARIZE_WORD_THRESHOLD || unsummarized.isEmpty()) return

        val textToSummarize = buildString {
            cachedSummary?.let {
                appendLine("Предыдущее краткое содержание:")
                appendLine(it)
                appendLine()
            }
            unsummarized.forEach { msg ->
                appendLine("${if (msg.role == ChatRole.USER) "Пользователь" else "Ассистент"}: ${msg.content}")
            }
        }

        val summaryPrompt = "Сделай краткий пересказ этой переписки, сохрани все важные факты, цифры и договорённости. Отвечай только пересказом без вступлений."

        val newSummary = claudeApi.call(
            apiKey = apiKey,
            context = summaryPrompt,
            history = listOf(
                ChatMessage(
                    id = "summary_request",
                    role = ChatRole.USER,
                    content = textToSummarize,
                    createdAt = Clock.System.now(),
                )
            ),
        )

        val lastId = unsummarized.last().id
        chatRepository.saveSummary(newSummary, lastId)
        cachedSummary = newSummary
        summarizedUpToMessageId = lastId
    }

    private fun getHistoryForApi(): List<ChatMessage> {
        val allMessages = _uiState.value.messages
        val unsummarized = if (summarizedUpToMessageId != null) {
            val idx = allMessages.indexOfFirst { it.id == summarizedUpToMessageId }
            if (idx >= 0) allMessages.drop(idx + 1) else allMessages
        } else {
            allMessages
        }

        val summaryMessage = cachedSummary?.let {
            ChatMessage(
                id = "summary",
                role = ChatRole.ASSISTANT,
                content = "[Краткое содержание предыдущей переписки]: $it",
                createdAt = Clock.System.now(),
            )
        }

        return listOfNotNull(summaryMessage) + unsummarized
    }


    @OptIn(ExperimentalEncodingApi::class, ExperimentalUuidApi::class)
    fun sendMessage(userText: String) {
        viewModelScope.launch {
            val attachment = _uiState.value.attachedFile
            var savedFilePath: String? = null
            var finalUserText = userText

            if (attachment != null) {
                savedFilePath = fileStorage.saveFile(attachment.name, attachment.bytes)

                if (isTextExtractable(attachment.mimeType, attachment.name)) {
                    val extracted = DocumentTextExtractor.extractText(
                        attachment.bytes, attachment.mimeType, attachment.name
                    )
                    if (!extracted.isNullOrBlank()) {
                        finalUserText = buildString {
                            appendLine(userText)
                            appendLine()
                            appendLine("[Содержимое файла ${attachment.name}]:")
                            append(extracted.take(8000)) // ограничим на всякий случай
                        }
                    }
                }
            }

            val userMessage = ChatMessage(
                id = Uuid.random().toString(),
                role = ChatRole.USER,
                content = userText, // в UI показываем оригинальный текст без содержимого файла
                createdAt = Clock.System.now(),
                attachedFilePath = savedFilePath,
                attachedFileName = attachment?.name,
                attachedFileMimeType = attachment?.mimeType,
            )
            chatRepository.insertMessage(userMessage)

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    attachedFile = null,
                    attachState = AttachState.NONE,
                )
            }

            try {
                maybeSummarize()

                val context = buildContext()
                val historyForApi = getHistoryForApi().dropLast(1) + userMessage.copy(content = finalUserText)

                val isImageOrPdf = attachment != null &&
                        (attachment.mimeType.startsWith("image/") || attachment.mimeType == "application/pdf")

                val fileBase64 = attachment?.takeIf { isImageOrPdf }
                    ?.let { Base64.encode(it.bytes) }

                val response = claudeApi.call(
                    apiKey = apiKey,
                    context = context,
                    history = historyForApi,
                    fileBase64 = fileBase64,
                    fileMimeType = attachment?.mimeType,
                )

                val assistantMessage = ChatMessage(
                    id = Uuid.random().toString(),
                    role = ChatRole.ASSISTANT,
                    content = response,
                    createdAt = Clock.System.now(),
                )
                chatRepository.insertMessage(assistantMessage)

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun buildContext(): String {
        val accounts = accountRepository.getAllAccounts().first()
        val userProfile = settingsRepository.getUserProfile().first()
        val taxProfile = settingsRepository.getTaxProfile().first()
        val rates = exchangeRateRepository.rates.value

        val userDataLabel = getString(Res.string.consultant_userdata)
        val nameLabel = getString(Res.string.consultant_name)
        val nameUnknown = getString(Res.string.consultant_name_unknown)
        val residenceLabel = getString(Res.string.consultant_country_of_residence)
        val citizenshipLabel = getString(Res.string.consultant_citizenship)
        val taxUserDataLabel = getString(Res.string.consultant_taxUserdata)
        val taxResidencyLabel = getString(Res.string.consultant_taxCitizenship)
        val settingsLabel = getString(Res.string.consultant_settings)
        val accountsLabel = getString(Res.string.consultant_accounts)
        val currencyLabel = getString(Res.string.consultant_currency)

        return buildString {
            appendLine(userDataLabel)
            appendLine("$nameLabel ${userProfile.name.ifBlank { nameUnknown }}")
            appendLine("$residenceLabel ${userProfile.countryOfResidence.label}")
            appendLine("$citizenshipLabel ${userProfile.citizenship.label}")
            appendLine()
            appendLine(taxUserDataLabel)
            appendLine("$taxResidencyLabel ${taxProfile.taxResidency.label}")
            appendLine("$settingsLabel ${taxProfile.countryTaxSettings}")
            appendLine()
            appendLine(accountsLabel)
            accounts.forEach { account ->
                appendLine("${account.name} (${account.bankName}): ${account.balance} ${account.currency.code}")
            }
            appendLine()
            appendLine(currencyLabel)
            rates.entries.take(15).forEach { (code, rate) ->
                appendLine("$code: $rate")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearChat() {
        _uiState.update { it.copy(messages = emptyList()) }
    }

    fun startSpeechRecognition() {
        _uiState.update { it.copy(isListening = true) }
    }

    fun onSpeechResult(text: String) {
        _uiState.update { it.copy(isListening = false) }
        if (text.isNotBlank()) sendMessage(text)
    }

    fun onSpeechError() {
        _uiState.update { it.copy(isListening = false) }
    }

    fun onFileAttached(file: AttachedFile) {
        _uiState.update { it.copy(attachedFile = file, attachState = AttachState.SUCCESS) }
    }

    fun onFileAttachFailed() {
        _uiState.update { it.copy(attachedFile = null, attachState = AttachState.FAILED) }
    }

    fun clearAttachment() {
        _uiState.update { it.copy(attachedFile = null, attachState = AttachState.NONE) }
    }
    companion object{
        private const val SUMMARIZE_WORD_THRESHOLD = 10_000
    }
}
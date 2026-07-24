package com.sleeplessdog.banquerito.presentation.consultant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplessdog.banquerito.data.remote.ClaudeApi
import com.sleeplessdog.banquerito.data.repository.AccountRepository
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ConsultantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val error: String? = null,
)

class ConsultantViewModel(
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val claudeApi: ClaudeApi,
    private val apiKey: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsultantUiState())
    val uiState: StateFlow<ConsultantUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalUuidApi::class)
    fun sendMessage(userText: String) {
        viewModelScope.launch {
            val userMessage = ChatMessage(
                id = Uuid.random().toString(),
                role = ChatRole.USER,
                content = userText,
                createdAt = Clock.System.now()
            )
            _uiState.update {
                it.copy(
                    messages = it.messages + userMessage,
                    isLoading = true,
                    error = null
                )
            }

            try {
                val context = buildContext()
                val history = _uiState.value.messages
                val response = claudeApi.call(apiKey = apiKey, context = context, history = history)

                val assistantMessage = ChatMessage(
                    id = Uuid.random().toString(),
                    role = ChatRole.ASSISTANT,
                    content = response,
                    createdAt = Clock.System.now()
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + assistantMessage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    private suspend fun buildContext(): String {
        val accounts = accountRepository.getAllAccounts().first()
        val userProfile = settingsRepository.getUserProfile().first()
        val taxProfile = settingsRepository.getTaxProfile().first()
        val rates = exchangeRateRepository.rates.value

        return buildString {
            appendLine("=== ДАННЫЕ ПОЛЬЗОВАТЕЛЯ ===")
            appendLine("Имя: ${userProfile.name.ifBlank { "не указано" }}")
            appendLine("Страна проживания: ${userProfile.countryOfResidence.label}")
            appendLine("Гражданство: ${userProfile.citizenship.label}")
            appendLine()
            appendLine("=== НАЛОГОВЫЙ ПРОФИЛЬ ===")
            appendLine("Налоговое резиденство: ${taxProfile.taxResidency.label}")
            appendLine("Настройки: ${taxProfile.countryTaxSettings}")
            appendLine()
            appendLine("=== СЧЕТА ===")
            accounts.forEach { account ->
                appendLine("${account.name} (${account.bankName}): ${account.balance} ${account.currency.code}")
            }
            appendLine()
            appendLine("=== КУРСЫ ВАЛЮТ (база EUR) ===")
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
}
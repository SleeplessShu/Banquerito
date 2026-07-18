package com.sleeplessdog.banquerito.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplessdog.banquerito.data.repository.AccountRepository
import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.SimReminderInterval
import com.sleeplessdog.banquerito.domain.model.Transaction
import com.sleeplessdog.banquerito.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val selectedCurrency: Currency = Currency.EUR,
    val isLoading: Boolean = false,
)

data class AccountDetailUiState(
    val account: Account? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
)

class AccountsViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(AccountDetailUiState())
    val detailUiState: StateFlow<AccountDetailUiState> = _detailUiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllAccounts().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts, isLoading = false) }
            }
        }
    }

    fun loadAccountDetail(accountId: String) {
        viewModelScope.launch {
            val account = uiState.value.accounts.find { it.id == accountId }
            _detailUiState.update { it.copy(account = account, isLoading = true) }
            repository.getTransactionsByAccount(accountId).collect { transactions ->
                _detailUiState.update { it.copy(transactions = transactions, isLoading = false) }
            }
        }
    }

    fun setDisplayCurrency(currency: Currency) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addTransaction(
        accountId: String,
        type: TransactionType,
        amount: Double,
        comment: String,
        date: LocalDate,
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                id = Uuid.random().toString(),
                accountId = accountId,
                type = type,
                amount = amount,
                comment = comment,
                date = date,
            )
            repository.insertTransaction(transaction)

            val account = uiState.value.accounts.find { it.id == accountId } ?: return@launch
            val delta = if (type == TransactionType.INCOME) amount else -amount
            repository.updateAccountBalance(accountId, account.balance + delta)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addAccount(name: String, bankName: String, currency: Currency, simReminderInterval: SimReminderInterval = SimReminderInterval.NEVER) {
        viewModelScope.launch {
            val account = Account(
                id = Uuid.random().toString(),
                name = name,
                bankName = bankName,
                balance = 0.0,
                currency = currency,
                simReminderInterval = simReminderInterval,
            )
            repository.insertAccount(account)
        }
    }

    fun renameAccount(accountId: String, newName: String) {
        viewModelScope.launch {
            repository.updateAccountName(accountId, newName)
        }
    }
}
package com.sleeplessdog.banquerito.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplessdog.banquerito.data.repository.AccountRepository
import com.sleeplessdog.banquerito.data.repository.ExchangeRateRepository
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
    val exchangeRates: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = false,
)

data class AccountDetailUiState(
    val account: Account? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
)

class AccountsViewModel(
    private val repository: AccountRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(AccountDetailUiState())
    val detailUiState: StateFlow<AccountDetailUiState> = _detailUiState.asStateFlow()


    init {
        loadAccounts()
        viewModelScope.launch {
            exchangeRateRepository.refresh()
            exchangeRateRepository.rates.collect { rates ->
                _uiState.update { it.copy(exchangeRates = rates) }
            }
        }
    }

    fun convertCurrency(amount: Double, from: String, to: String): Double =
        exchangeRateRepository.convert(amount, from, to)

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
            _detailUiState.update { it.copy(isLoading = true) }
            launch {
                repository.getAccountById(accountId).collect { account ->
                    _detailUiState.update { it.copy(account = account) }
                }
            }
            launch {
                repository.getTransactionsByAccount(accountId).collect { transactions ->
                    _detailUiState.update {
                        it.copy(
                            transactions = transactions, isLoading = false
                        )
                    }
                }
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
    fun addAccount(
        name: String,
        bankName: String,
        currency: Currency,
        simReminderInterval: SimReminderInterval = SimReminderInterval.NEVER,
    ) {
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

    fun updateAccountBank(accountId: String, bankName: String) {
        viewModelScope.launch {
            repository.updateAccountBank(accountId, bankName)
        }
    }

    fun updateSimReminder(accountId: String, interval: SimReminderInterval) {
        viewModelScope.launch {
            repository.updateSimReminder(accountId, interval, null)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction.id)
            val account = detailUiState.value.account ?: return@launch
            val delta =
                if (transaction.type == TransactionType.INCOME) -transaction.amount else transaction.amount
            repository.updateAccountBalance(account.id, account.balance + delta)
            if ((transaction.type == TransactionType.TRANSFER_EXPENSE || transaction.type == TransactionType.TRANSFER_INCOME) && transaction.toAccountId != null) {
                val toAccount = uiState.value.accounts.find { it.id == transaction.toAccountId }
                if (toAccount != null) {
                    repository.updateAccountBalance(
                        toAccount.id, toAccount.balance - transaction.amount
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addTransfer(
        fromAccountId: String,
        toAccountId: String,
        amount: Double,
        comment: String,
        date: LocalDate,
    ) {
        viewModelScope.launch {

            val outTransaction = Transaction(
                id = Uuid.random().toString(),
                accountId = fromAccountId,
                type = TransactionType.TRANSFER_EXPENSE,
                amount = amount,
                comment = comment,
                date = date,
                toAccountId = toAccountId
            )

            val inTransaction = Transaction(
                id = Uuid.random().toString(),
                accountId = toAccountId,
                type = TransactionType.TRANSFER_INCOME,
                amount = amount,
                comment = comment,
                date = date,
                toAccountId = fromAccountId
            )
            repository.insertTransaction(outTransaction)
            repository.insertTransaction(inTransaction)

            val fromAccount =
                uiState.value.accounts.find { it.id == fromAccountId } ?: return@launch
            val toAccount = uiState.value.accounts.find { it.id == toAccountId } ?: return@launch
            repository.updateAccountBalance(fromAccountId, fromAccount.balance - amount)
            repository.updateAccountBalance(toAccountId, toAccount.balance + amount)
        }
    }
}
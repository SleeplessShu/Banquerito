package com.sleeplessdog.banquerito.testutil

import com.sleeplessdog.banquerito.data.interfaces.IAccountRepository
import com.sleeplessdog.banquerito.domain.model.*

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

import kotlinx.datetime.LocalDate


class FakeAccountRepository : IAccountRepository {

    val accountsFlow = MutableStateFlow<List<Account>>(emptyList())
    val transactionsByAccount = mutableMapOf<String, MutableStateFlow<List<Transaction>>>()

    // трекеры вызовов для проверок в тестах
    val insertedAccounts = mutableListOf<Account>()
    val insertedTransactions = mutableListOf<Transaction>()
    val deletedTransactionIds = mutableListOf<String>()
    val updatedNames = mutableMapOf<String, String>()
    val updatedBanks = mutableMapOf<String, String>()
    val updatedBalances = mutableMapOf<String, Double>()
    val updatedSimReminders = mutableMapOf<String, SimReminderInterval>()

    override fun getAllAccounts(): Flow<List<Account>> = accountsFlow.asStateFlow()

    override fun getAccountById(id: String): Flow<Account?> =
        accountsFlow.map { list -> list.find { it.id == id } }

    override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
        transactionsByAccount.getOrPut(accountId) { MutableStateFlow(emptyList()) }.asStateFlow()

    override suspend fun insertAccount(account: Account) {
        insertedAccounts.add(account)
        accountsFlow.value = accountsFlow.value + account
    }

    override suspend fun updateAccountName(id: String, name: String) {
        updatedNames[id] = name
        accountsFlow.value = accountsFlow.value.map {
            if (it.id == id) it.copy(name = name) else it
        }
    }

    override suspend fun updateAccountBank(id: String, bankName: String) {
        updatedBanks[id] = bankName
        accountsFlow.value = accountsFlow.value.map {
            if (it.id == id) it.copy(bankName = bankName) else it
        }
    }

    override suspend fun updateAccountBalance(id: String, balance: Double) {
        updatedBalances[id] = balance
        accountsFlow.value = accountsFlow.value.map {
            if (it.id == id) it.copy(balance = balance) else it
        }
    }

    override suspend fun updateSimReminder(id: String, interval: SimReminderInterval, lastDate: LocalDate?) {
        updatedSimReminders[id] = interval
        accountsFlow.value = accountsFlow.value.map {
            if (it.id == id) it.copy(simReminderInterval = interval, simReminderLastDate = lastDate) else it
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        insertedTransactions.add(transaction)
        val flow = transactionsByAccount.getOrPut(transaction.accountId) { MutableStateFlow(emptyList()) }
        flow.value = flow.value + transaction
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val flow = transactionsByAccount.getOrPut(transaction.accountId) { MutableStateFlow(emptyList()) }
        flow.value = flow.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun deleteTransaction(id: String) {
        deletedTransactionIds.add(id)
        transactionsByAccount.values.forEach { flow ->
            flow.value = flow.value.filterNot { it.id == id }
        }
    }

    fun setAccounts(accounts: List<Account>) {
        accountsFlow.value = accounts
    }

    fun setTransactions(accountId: String, transactions: List<Transaction>) {
        transactionsByAccount.getOrPut(accountId) { MutableStateFlow(emptyList()) }.value = transactions
    }
}
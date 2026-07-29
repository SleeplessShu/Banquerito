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

    override fun getAllAccounts(): Flow<List<Account>> = accountsFlow.asStateFlow()

    override fun getAccountById(id: String): Flow<Account?> =
        accountsFlow.map { list -> list.find { it.id == id } }

    override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
        transactionsByAccount.getOrPut(accountId) { MutableStateFlow(emptyList()) }.asStateFlow()

    override suspend fun insertAccount(account: Account) {}
    override suspend fun updateAccountName(id: String, name: String) {}
    override suspend fun updateAccountBank(id: String, bankName: String) {}
    override suspend fun updateAccountBalance(id: String, balance: Double) {}
    override suspend fun updateSimReminder(id: String, interval: SimReminderInterval, lastDate: LocalDate?) {}
    override suspend fun insertTransaction(transaction: Transaction) {}
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(id: String) {}

    fun setAccounts(accounts: List<Account>) {
        accountsFlow.value = accounts
    }

    fun setTransactions(accountId: String, transactions: List<Transaction>) {
        transactionsByAccount.getOrPut(accountId) { MutableStateFlow(emptyList()) }.value = transactions
    }
}
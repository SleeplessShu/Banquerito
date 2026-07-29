package com.sleeplessdog.banquerito.data.interfaces

import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.SimReminderInterval
import com.sleeplessdog.banquerito.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface IAccountRepository {

    fun getAllAccounts(): Flow<List<Account>>

    fun getAccountById(id: String): Flow<Account?>

    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>>

    suspend fun insertAccount(account: Account)

    suspend fun updateAccountName(id: String, name: String)

    suspend fun updateAccountBank(id: String, bankName: String)

    suspend fun updateAccountBalance(id: String, balance: Double)

    suspend fun updateSimReminder(id: String, interval: SimReminderInterval, lastDate: LocalDate?)

    suspend fun insertTransaction(transaction: Transaction)

    suspend fun updateTransaction(transaction: Transaction)

    suspend fun deleteTransaction(id: String)
}
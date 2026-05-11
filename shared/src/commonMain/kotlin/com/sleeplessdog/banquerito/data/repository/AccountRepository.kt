package com.sleeplessdog.banquerito.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.sleeplessdog.banquerito.db.BanqueritoDB
import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.SimReminderInterval
import com.sleeplessdog.banquerito.domain.model.Transaction
import com.sleeplessdog.banquerito.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class AccountRepository(
    private val db: BanqueritoDB

) {

    fun getAllAccounts(): Flow<List<Account>> =
        db.banqueritoDBQueries.selectAllAccounts()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toAccount() } }

    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
        db.banqueritoDBQueries.selectTransactionsByAccount(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toTransaction() } }

    suspend fun insertAccount(account: Account) {
        db.banqueritoDBQueries.insertAccount(
            id = account.id,
            name = account.name,
            bank_name = account.bankName,
            balance = account.balance,
            currency_code = account.currency.code,
            sim_reminder_interval = account.simReminderInterval.name,
            sim_reminder_last_date = account.simReminderLastDate?.toString()
        )
    }

    suspend fun updateAccountName(id: String, name: String) {
        db.banqueritoDBQueries.updateAccountName(name = name, id = id)
    }

    suspend fun updateAccountBalance(id: String, balance: Double) {
        db.banqueritoDBQueries.updateAccountBalance(balance = balance, id = id)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        db.banqueritoDBQueries.insertTransaction(
            id = transaction.id,
            account_id = transaction.accountId,
            type = transaction.type.name,
            amount = transaction.amount,
            comment = transaction.comment,
            date = transaction.date.toString()
        )
    }

    suspend fun deleteTransaction(id: String) {
        db.banqueritoDBQueries.deleteTransaction(id)
    }

    suspend fun updateSimReminder(id: String, interval: SimReminderInterval, lastDate: LocalDate?) {
        db.banqueritoDBQueries.updateSimReminder(
            sim_reminder_interval = interval.name,
            sim_reminder_last_date = lastDate?.toString(),
            id = id
        )
    }
}

private fun com.sleeplessdog.banquerito.db.Account.toAccount() = Account(
    id = id,
    name = name,
    bankName = bank_name,
    balance = balance,
    currency = Currency.entries.find { it.code == currency_code } ?: Currency.EUR,
    simReminderInterval = SimReminderInterval.valueOf(sim_reminder_interval),
    simReminderLastDate = sim_reminder_last_date?.let { LocalDate.parse(it) }
)

private fun com.sleeplessdog.banquerito.db.Transaction_.toTransaction() = Transaction(
    id = id,
    accountId = account_id,
    type = TransactionType.valueOf(type),
    amount = amount,
    comment = comment,
    date = LocalDate.parse(date)
)
package com.sleeplessdog.banquerito.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Transaction(
    val id: String,
    val accountId: String,
    val type: TransactionType,
    val amount: Double,
    val comment: String,
    val date: LocalDate,
    val toAccountId: String? = null,
    val createdAt: Instant = Clock.System.now()
)

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER_EXPENSE,
    TRANSFER_INCOME
}

enum class TransactionFilter {
    ALL, INCOME, EXPENSE, TRANSFER
}
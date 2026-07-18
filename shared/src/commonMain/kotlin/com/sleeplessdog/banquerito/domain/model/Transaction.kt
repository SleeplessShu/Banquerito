package com.sleeplessdog.banquerito.domain.model

import kotlinx.datetime.LocalDate

data class Transaction(
    val id: String,
    val accountId: String,
    val type: TransactionType,
    val amount: Double,
    val comment: String,
    val date: LocalDate,
)

enum class TransactionType {
    INCOME,
    EXPENSE
}
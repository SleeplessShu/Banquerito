package com.sleeplessdog.banquerito.domain.model

import kotlinx.datetime.LocalDate

sealed class PlannedItem {
    abstract val id: String
    abstract val amount: Double
    abstract val currency: Currency
    abstract val accountId: String
    abstract val recurrence: Recurrence
    abstract val nextDate: LocalDate
    abstract val isArchived: Boolean

    data class Payment(val payment: PlannedPayment) : PlannedItem() {
        override val id = payment.id
        override val amount = payment.amount
        override val currency = payment.currency
        override val accountId = payment.accountId
        override val recurrence = payment.recurrence
        override val nextDate = payment.nextDate
        override val isArchived = payment.isArchived
        val name = payment.name
        val remindDaysBefore = payment.remindDaysBefore
    }

    data class Income(val income: PlannedIncome) : PlannedItem() {
        override val id = income.id
        override val amount = income.amount
        override val currency = income.currency
        override val accountId = income.accountId
        override val recurrence = income.recurrence
        override val nextDate = income.nextDate
        override val isArchived = income.isArchived
        val comment = income.comment
    }
}
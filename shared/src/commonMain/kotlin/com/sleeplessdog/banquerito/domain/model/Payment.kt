package com.sleeplessdog.banquerito.domain.model

import kotlinx.datetime.LocalDate

data class PlannedPayment(
    val id: String,
    val name: String,
    val amount: Double,
    val currency: Currency,
    val accountId: String,
    val recurrence: Recurrence,
    val dayOfMonth: Int,
    val nextDate: LocalDate,
    val remindDaysBefore: Int,
    val isActive: Boolean,
)

enum class Recurrence {
    ONCE,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY,
}
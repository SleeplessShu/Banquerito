package com.sleeplessdog.banquerito.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class PlannedIncome(
    val id: String,
    val comment: String,
    val amount: Double,
    val currency: Currency,
    val accountId: String,
    val recurrence: Recurrence,
    val nextDate: LocalDate,
    val isArchived: Boolean = false,
    val archivedAt: Instant? = null,
)
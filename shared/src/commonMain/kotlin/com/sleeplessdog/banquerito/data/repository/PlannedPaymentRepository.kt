package com.sleeplessdog.banquerito.data.repository

import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.sleeplessdog.banquerito.db.BanqueritoDB
import com.sleeplessdog.banquerito.domain.model.Citizenship
import com.sleeplessdog.banquerito.domain.model.CountryOfResidence
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.PlannedIncome
import com.sleeplessdog.banquerito.domain.model.PlannedPayment
import com.sleeplessdog.banquerito.domain.model.Recurrence
import com.sleeplessdog.banquerito.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class PlannedPaymentRepository(private val db: BanqueritoDB) {

    fun getAllPlannedPayments(): Flow<List<PlannedPayment>> =
        db.banqueritoDBQueries.selectAllPlannedPayments()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toPlannedPayment() } }

    fun getArchivedPlannedPayments(): Flow<List<PlannedPayment>> =
        db.banqueritoDBQueries.selectArchivedPlannedPayments()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toPlannedPayment() } }

    suspend fun insertPlannedPayment(payment: PlannedPayment) {
        db.banqueritoDBQueries.insertPlannedPayment(
            id = payment.id,
            name = payment.name,
            amount = payment.amount,
            currency_code = payment.currency.code,
            account_id = payment.accountId,
            recurrence = payment.recurrence.name,
            day_of_month = payment.dayOfMonth.toLong(),
            next_date = payment.nextDate.toString(),
            remind_days_before = payment.remindDaysBefore.toLong(),
            is_active = if (payment.isActive) 1L else 0L,
            is_archived = if (payment.isArchived) 1L else 0L,
            archived_at = payment.archivedAt?.toString()
        )
    }

    suspend fun updatePlannedPayment(payment: PlannedPayment) {
        db.banqueritoDBQueries.updatePlannedPayment(
            name = payment.name,
            amount = payment.amount,
            currency_code = payment.currency.code,
            account_id = payment.accountId,
            recurrence = payment.recurrence.name,
            day_of_month = payment.dayOfMonth.toLong(),
            next_date = payment.nextDate.toString(),
            remind_days_before = payment.remindDaysBefore.toLong(),
            is_active = if (payment.isActive) 1L else 0L,
            id = payment.id
        )
    }

    suspend fun archivePlannedPayment(id: String, archivedAt: Instant) {
        db.banqueritoDBQueries.archivePlannedPayment(
            archived_at = archivedAt.toString(),
            id = id
        )
    }

    suspend fun deletePlannedPayment(id: String) {
        db.banqueritoDBQueries.deletePlannedPayment(id)
    }

    fun getUserProfile(): Flow<UserProfile?> =
        db.banqueritoDBQueries.selectUserProfile()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toUserProfile() }

    suspend fun upsertUserProfile(profile: UserProfile) {
        db.banqueritoDBQueries.upsertUserProfile(
            name = profile.name,
            country_of_residence = profile.countryOfResidence.name,
            citizenship = profile.citizenship.name,
            default_currency = profile.defaultCurrency.code
        )
    }

    fun getAllPlannedIncomes(): Flow<List<PlannedIncome>> =
        db.banqueritoDBQueries.selectAllPlannedIncomes()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toPlannedIncome() } }

    fun getArchivedPlannedIncomes(): Flow<List<PlannedIncome>> =
        db.banqueritoDBQueries.selectArchivedPlannedIncomes()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toPlannedIncome() } }

    suspend fun insertPlannedIncome(income: PlannedIncome) {
        db.banqueritoDBQueries.insertPlannedIncome(
            id = income.id,
            comment = income.comment,
            amount = income.amount,
            currency_code = income.currency.code,
            account_id = income.accountId,
            recurrence = income.recurrence.name,
            next_date = income.nextDate.toString(),
            is_archived = if (income.isArchived) 1L else 0L,
            archived_at = income.archivedAt?.toString()
        )
    }

    suspend fun updatePlannedIncome(income: PlannedIncome) {
        db.banqueritoDBQueries.updatePlannedIncome(
            comment = income.comment,
            amount = income.amount,
            currency_code = income.currency.code,
            account_id = income.accountId,
            recurrence = income.recurrence.name,
            next_date = income.nextDate.toString(),
            id = income.id
        )
    }

    suspend fun archivePlannedIncome(id: String, archivedAt: Instant) {
        db.banqueritoDBQueries.archivePlannedIncome(
            archived_at = archivedAt.toString(),
            id = id
        )
    }

    suspend fun deletePlannedIncome(id: String) {
        db.banqueritoDBQueries.deletePlannedIncome(id)
    }
}

private fun com.sleeplessdog.banquerito.db.PlannedIncome.toPlannedIncome() = PlannedIncome(
    id = id,
    comment = comment,
    amount = amount,
    currency = Currency.entries.find { it.code == currency_code } ?: Currency.EUR,
    accountId = account_id,
    recurrence = Recurrence.valueOf(recurrence),
    nextDate = LocalDate.parse(next_date),
    isArchived = is_archived == 1L,
    archivedAt = archived_at?.let { Instant.parse(it) }
)

private fun com.sleeplessdog.banquerito.db.PlannedPayment.toPlannedPayment() = PlannedPayment(
    id = id,
    name = name,
    amount = amount,
    currency = Currency.entries.find { it.code == currency_code } ?: Currency.EUR,
    accountId = account_id,
    recurrence = Recurrence.valueOf(recurrence),
    dayOfMonth = day_of_month.toInt(),
    nextDate = LocalDate.parse(next_date),
    remindDaysBefore = remind_days_before.toInt(),
    isActive = is_active == 1L,
    isArchived = is_archived == 1L,
    archivedAt = archived_at?.let { Instant.parse(it) }
)

private fun com.sleeplessdog.banquerito.db.UserProfile.toUserProfile() = UserProfile(
    name = name,
    countryOfResidence = CountryOfResidence.valueOf(country_of_residence),
    citizenship = Citizenship.valueOf(citizenship),
    defaultCurrency = Currency.entries.find { it.code == default_currency } ?: Currency.EUR
)


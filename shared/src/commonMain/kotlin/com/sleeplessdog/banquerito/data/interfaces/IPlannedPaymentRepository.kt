package com.sleeplessdog.banquerito.data.interfaces

import com.sleeplessdog.banquerito.domain.model.PlannedIncome
import com.sleeplessdog.banquerito.domain.model.PlannedPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface IPlannedPaymentRepository {

    fun getAllPlannedPayments(): Flow<List<PlannedPayment>>

    fun getArchivedPlannedPayments(): Flow<List<PlannedPayment>>

    suspend fun insertPlannedPayment(payment: PlannedPayment)

    suspend fun updatePlannedPayment(payment: PlannedPayment)

    suspend fun archivePlannedPayment(id: String, archivedAt: Instant)

    suspend fun deletePlannedPayment(id: String)

    fun getAllPlannedIncomes(): Flow<List<PlannedIncome>>

    fun getArchivedPlannedIncomes(): Flow<List<PlannedIncome>>

    suspend fun insertPlannedIncome(income: PlannedIncome)

    suspend fun updatePlannedIncome(income: PlannedIncome)

    suspend fun archivePlannedIncome(id: String, archivedAt: Instant)

    suspend fun deletePlannedIncome(id: String)
}
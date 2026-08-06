package com.sleeplessdog.banquerito.testutil

import com.sleeplessdog.banquerito.data.interfaces.IPlannedPaymentRepository
import com.sleeplessdog.banquerito.domain.model.PlannedIncome
import com.sleeplessdog.banquerito.domain.model.PlannedPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant

class FakePlannedPaymentRepository : IPlannedPaymentRepository {

    val paymentsFlow = MutableStateFlow<List<PlannedPayment>>(emptyList())
    val archivedPaymentsFlow = MutableStateFlow<List<PlannedPayment>>(emptyList())
    val incomesFlow = MutableStateFlow<List<PlannedIncome>>(emptyList())
    val archivedIncomesFlow = MutableStateFlow<List<PlannedIncome>>(emptyList())

    val insertedPayments = mutableListOf<PlannedPayment>()
    val updatedPayments = mutableListOf<PlannedPayment>()
    val archivedPaymentIds = mutableListOf<String>()
    val deletedPaymentIds = mutableListOf<String>()

    val insertedIncomes = mutableListOf<PlannedIncome>()
    val updatedIncomes = mutableListOf<PlannedIncome>()
    val archivedIncomeIds = mutableListOf<String>()
    val deletedIncomeIds = mutableListOf<String>()

    override fun getAllPlannedPayments(): Flow<List<PlannedPayment>> = paymentsFlow.asStateFlow()
    override fun getArchivedPlannedPayments(): Flow<List<PlannedPayment>> = archivedPaymentsFlow.asStateFlow()

    override suspend fun insertPlannedPayment(payment: PlannedPayment) {
        insertedPayments.add(payment)
        paymentsFlow.value = paymentsFlow.value + payment
    }

    override suspend fun updatePlannedPayment(payment: PlannedPayment) {
        updatedPayments.add(payment)
        paymentsFlow.value = paymentsFlow.value.map { if (it.id == payment.id) payment else it }
    }

    override suspend fun archivePlannedPayment(id: String, archivedAt: Instant) {
        archivedPaymentIds.add(id)
        val payment = paymentsFlow.value.find { it.id == id }
        if (payment != null) {
            paymentsFlow.value = paymentsFlow.value.filterNot { it.id == id }
            archivedPaymentsFlow.value = archivedPaymentsFlow.value + payment.copy(isArchived = true, archivedAt = archivedAt)
        }
    }

    override suspend fun deletePlannedPayment(id: String) {
        deletedPaymentIds.add(id)
        paymentsFlow.value = paymentsFlow.value.filterNot { it.id == id }
        archivedPaymentsFlow.value = archivedPaymentsFlow.value.filterNot { it.id == id }
    }

    override fun getAllPlannedIncomes(): Flow<List<PlannedIncome>> = incomesFlow.asStateFlow()
    override fun getArchivedPlannedIncomes(): Flow<List<PlannedIncome>> = archivedIncomesFlow.asStateFlow()

    override suspend fun insertPlannedIncome(income: PlannedIncome) {
        insertedIncomes.add(income)
        incomesFlow.value = incomesFlow.value + income
    }

    override suspend fun updatePlannedIncome(income: PlannedIncome) {
        updatedIncomes.add(income)
        incomesFlow.value = incomesFlow.value.map { if (it.id == income.id) income else it }
    }

    override suspend fun archivePlannedIncome(id: String, archivedAt: Instant) {
        archivedIncomeIds.add(id)
        val income = incomesFlow.value.find { it.id == id }
        if (income != null) {
            incomesFlow.value = incomesFlow.value.filterNot { it.id == id }
            archivedIncomesFlow.value = archivedIncomesFlow.value + income.copy(isArchived = true, archivedAt = archivedAt)
        }
    }

    override suspend fun deletePlannedIncome(id: String) {
        deletedIncomeIds.add(id)
        incomesFlow.value = incomesFlow.value.filterNot { it.id == id }
        archivedIncomesFlow.value = archivedIncomesFlow.value.filterNot { it.id == id }
    }
}
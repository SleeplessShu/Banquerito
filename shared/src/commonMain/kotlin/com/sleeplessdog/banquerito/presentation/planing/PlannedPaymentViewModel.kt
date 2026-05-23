package com.sleeplessdog.banquerito.presentation.planing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplessdog.banquerito.data.repository.PlannedPaymentRepository
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.PlannedPayment
import com.sleeplessdog.banquerito.domain.model.PlanningUiState
import com.sleeplessdog.banquerito.domain.model.Recurrence
import com.sleeplessdog.banquerito.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PlannedPaymentViewModel(
    private val repository: PlannedPaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanningUiState())
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    init {
        loadPayments()
        loadArchived()
        loadUserProfile()
    }

    private fun loadPayments() {
        viewModelScope.launch {
            repository.getAllPlannedPayments().collect { payments ->
                _uiState.update { it.copy(payments = payments) }
            }
        }
    }

    private fun loadArchived() {
        viewModelScope.launch {
            repository.getArchivedPlannedPayments().collect { archived ->
                _uiState.update { it.copy(archivedPayments = archived) }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            repository.getUserProfile().collect { profile ->
                _uiState.update { it.copy(userProfile = profile ?: UserProfile()) }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addPayment(
        name: String,
        amount: Double,
        currency: Currency,
        accountId: String,
        recurrence: Recurrence,
        dayOfMonth: Int,
        nextDate: LocalDate,
        remindDaysBefore: Int,
    ) {
        viewModelScope.launch {
            val payment = PlannedPayment(
                id = Uuid.Companion.random().toString(),
                name = name,
                amount = amount,
                currency = currency,
                accountId = accountId,
                recurrence = recurrence,
                dayOfMonth = dayOfMonth,
                nextDate = nextDate,
                remindDaysBefore = remindDaysBefore,
                isActive = true,
                isArchived = false,
                archivedAt = null,
            )
            repository.insertPlannedPayment(payment)
        }
    }

    fun updatePayment(payment: PlannedPayment) {
        viewModelScope.launch {
            repository.updatePlannedPayment(payment)
        }
    }

    fun archivePayment(payment: PlannedPayment) {
        viewModelScope.launch {
            repository.archivePlannedPayment(payment.id, Clock.System.now())
        }
    }

    fun deletePayment(payment: PlannedPayment) {
        viewModelScope.launch {
            repository.deletePlannedPayment(payment.id)
        }
    }

    fun toggleArchiveVisible() {
        _uiState.update { it.copy(showArchive = !it.showArchive) }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.upsertUserProfile(profile)
        }
    }
}
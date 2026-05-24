package com.sleeplessdog.banquerito.domain.model

data class PlanningUiState(
    val payments: List<PlannedPayment> = emptyList(),
    val incomes: List<PlannedIncome> = emptyList(),
    val archivedPayments: List<PlannedPayment> = emptyList(),
    val archivedIncomes: List<PlannedIncome> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val showArchive: Boolean = false,
)
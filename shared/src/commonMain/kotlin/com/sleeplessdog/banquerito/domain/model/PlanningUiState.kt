package com.sleeplessdog.banquerito.domain.model

data class PlanningUiState(
    val payments: List<PlannedPayment> = emptyList(),
    val archivedPayments: List<PlannedPayment> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val showArchive: Boolean = false,
)

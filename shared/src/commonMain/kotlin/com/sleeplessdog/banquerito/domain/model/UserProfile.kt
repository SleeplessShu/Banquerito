package com.sleeplessdog.banquerito.domain.model

data class UserProfile(
    val citizenship: String = "",
    val taxResidency: String = "",
    val employmentStatus: EmploymentStatus = EmploymentStatus.EMPLOYEE,
    val autonomoRegime: AutonomoRegime = AutonomoRegime.GENERAL,
    val defaultCurrency: Currency = Currency.EUR,
)

enum class EmploymentStatus(val label: String) {
    EMPLOYEE("Наёмный сотрудник"),
    AUTONOMO("Autónomo"),
    NON_RESIDENT("Нерезидент"),
    OTHER("Другое"),
}

enum class AutonomoRegime(val label: String) {
    TARIFA_PLANA("Tarifa plana"),
    GENERAL("Régimen general"),
}
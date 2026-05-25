package com.sleeplessdog.banquerito.domain.model

data class UserProfile(
    val name: String = "",
    val countryOfResidence: CountryOfResidence = CountryOfResidence.SPAIN,
    val citizenship: Citizenship = Citizenship.OTHER,
    val defaultCurrency: Currency = Currency.EUR,
)

enum class CountryOfResidence(val label: String) {
    SPAIN("Испания"),
    SERBIA("Сербия"),
    ARMENIA("Армения"),
    OTHER("Другое"),
}

enum class Citizenship(val label: String) {
    UKRAINE("Украина"),
    BELARUS("Беларусь"),
    RUSSIA("Россия"),
    OTHER("Другое"),
}


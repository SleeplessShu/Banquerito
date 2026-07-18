package com.sleeplessdog.banquerito.domain.model

import kotlinx.datetime.LocalDate

data class Account(
    val id: String,
    val name: String,
    val bankName: String,
    val balance: Double,
    val currency: Currency,
    val simReminderInterval: SimReminderInterval = SimReminderInterval.NEVER,
    val simReminderLastDate: LocalDate? = null,
)

enum class SimReminderInterval(val label: String) {
    NEVER("Никогда"),
    MONTHLY("Месяц"),
    TWO_MONTHS("2 месяца"),
    THREE_MONTHS("3 месяца"),
}

enum class Currency(val symbol: String, val code: String) {
    EUR("€", "EUR"),
    GBP("£", "GBP"),
    RUB("₽", "RUB"),
    UAH("₴", "UAH"),
    BYN("Br", "BYN"),
    KZT("₸", "KZT"),
    UZS("сўм", "UZS"),
    AZN("₼", "AZN"),
    KGS("с", "KGS"),
    TJS("с.", "TJS"),
    TMT("T", "TMT"),
    MDL("L", "MDL"),
    GEL("₾", "GEL"),
    AMD("֏", "AMD"),
    TRY("₺", "TRY"),
    ILS("₪", "ILS"),
    USD("$", "USD"),
    CHF("Fr", "CHF"),
    NOK("kr", "NOK"),
    SEK("kr", "SEK"),
    DKK("kr", "DKK"),
    PLN("zł", "PLN"),
    CZK("Kč", "CZK"),
    HUF("Ft", "HUF"),
    RON("lei", "RON"),
    BGN("лв", "BGN"),
    HRK("kn", "HRK"),
    RSD("din", "RSD"),
}

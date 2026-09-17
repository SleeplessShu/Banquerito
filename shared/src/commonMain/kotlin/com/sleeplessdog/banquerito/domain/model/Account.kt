package com.sleeplessdog.banquerito.domain.model

import kotlinx.datetime.LocalDate
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

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

enum class Currency(
    val code: String,
    val symbol: String,
    val nameRes: StringResource,
) {
    EUR("EUR", "€",   Res.string.currency_eur),
    GBP("GBP", "£",   Res.string.currency_gbp),
    CHF("CHF", "Fr",  Res.string.currency_chf),
    PLN("PLN", "zł",  Res.string.currency_pln),
    CZK("CZK", "Kč",  Res.string.currency_czk),
    HUF("HUF", "Ft",  Res.string.currency_huf),
    RON("RON", "lei", Res.string.currency_ron),
    BGN("BGN", "лв",  Res.string.currency_bgn),
    TRY("TRY", "₺",   Res.string.currency_try),
    DKK("DKK", "kr",  Res.string.currency_dkk),
    SEK("SEK", "kr",  Res.string.currency_sek),
    NOK("NOK", "kr",  Res.string.currency_nok),
    ISK("ISK", "kr",  Res.string.currency_isk),
    MDL("MDL", "L",   Res.string.currency_mdl),
    GEL("GEL", "₾",   Res.string.currency_gel),
    AZN("AZN", "₼",   Res.string.currency_azn),
    MKD("MKD", "ден", Res.string.currency_mkd),
    ALL("ALL", "L",   Res.string.currency_all),
    BAM("BAM", "KM",  Res.string.currency_bam),
    RSD("RSD", "дин", Res.string.currency_rsd),

    // ── СНГ ───────────────────────────────────────────────────────────────────
    RUB("RUB", "₽",   Res.string.currency_rub),
    UAH("UAH", "₴",   Res.string.currency_uah),
    BYN("BYN", "Br",  Res.string.currency_byn),
    AMD("AMD", "֏",   Res.string.currency_amd),
    KZT("KZT", "₸",   Res.string.currency_kzt),
    UZS("UZS", "сум", Res.string.currency_uzs),
    TMT("TMT", "T",   Res.string.currency_tmt),
    KGS("KGS", "с",   Res.string.currency_kgs),
    TJS("TJS", "SM",  Res.string.currency_tjs),

    // ── Америка ───────────────────────────────────────────────────────────────
    USD("USD", "$",   Res.string.currency_usd),
    CAD("CAD", "C$",  Res.string.currency_cad),
    MXN("MXN", "$",   Res.string.currency_mxn),
    BRL("BRL", "R$",  Res.string.currency_brl),
    ARS("ARS", "$",   Res.string.currency_ars),
    CLP("CLP", "$",   Res.string.currency_clp),
    COP("COP", "$",   Res.string.currency_cop),
    PEN("PEN", "S/",  Res.string.currency_pen),
    VES("VES", "Bs",  Res.string.currency_ves),

    // ── Азия ──────────────────────────────────────────────────────────────────
    CNY("CNY", "¥",   Res.string.currency_cny),
    JPY("JPY", "¥",   Res.string.currency_jpy),
    KRW("KRW", "₩",   Res.string.currency_krw),
    INR("INR", "₹",   Res.string.currency_inr),
    THB("THB", "฿",   Res.string.currency_thb),
    VND("VND", "₫",   Res.string.currency_vnd),
    IDR("IDR", "Rp",  Res.string.currency_idr),
    PHP("PHP", "₱",   Res.string.currency_php),
    MYR("MYR", "RM",  Res.string.currency_myr),
    SGD("SGD", "S$",  Res.string.currency_sgd),
    MNT("MNT", "₮",   Res.string.currency_mnt),
    NPR("NPR", "रू",  Res.string.currency_npr),
    LKR("LKR", "₨",   Res.string.currency_lkr),

    // ── Океания ───────────────────────────────────────────────────────────────
    AUD("AUD", "A$",  Res.string.currency_aud),
    NZD("NZD", "NZ$", Res.string.currency_nzd),
}
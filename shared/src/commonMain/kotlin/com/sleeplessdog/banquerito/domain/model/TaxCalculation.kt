package com.sleeplessdog.banquerito.domain.model

data class TaxCalculation(
    val grossIncome: Double,
    val segments: List<TaxSegment>,
    val netIncome: Double,
    val currency: Currency,
)

data class TaxSegment(
    val label: String,
    val amount: Double,
    val percentOfGross: Float,
    val colorRole: TaxSegmentColor,
)

enum class TaxSegmentColor {
    NET_INCOME,
    IRPF,
    IVA,
    CUOTA,
    OTHER,
}

data class TaxDeadline(
    val label: String,
    val date: kotlinx.datetime.LocalDate,
)
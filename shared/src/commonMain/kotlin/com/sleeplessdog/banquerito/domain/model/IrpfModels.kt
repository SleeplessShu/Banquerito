package com.sleeplessdog.banquerito.domain.model

enum class IncomeExtrapolationMode(val label: String) {
    QUARTER_ONLY("Только текущий квартал"),
    EXTRAPOLATE_CURRENT_QUARTER("Квартал × 4"),
    EXTRAPOLATE_FROM_PREVIOUS("Накопленный доход с начала года"),
}

data class IrpfBracket(
    val from: Double,
    val to: Double,
    val rate: Double,
)

data class IrpfBracketAmount(
    val bracket: IrpfBracket,
    val taxedAmount: Double,
    val taxPaid: Double,
)

data class IrpfBreakdown(
    val annualIncomeUsed: Double,
    val totalTax: Double,
    val effectiveRate: Float,
    val bracketAmounts: List<IrpfBracketAmount>,
    val currentBracket: IrpfBracket,
    val amountToNextBracketMonthly: Double?,
)
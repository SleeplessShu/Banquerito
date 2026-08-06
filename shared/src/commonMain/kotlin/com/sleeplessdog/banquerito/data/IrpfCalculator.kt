package com.sleeplessdog.banquerito.data

import com.sleeplessdog.banquerito.domain.model.IncomeExtrapolationMode
import com.sleeplessdog.banquerito.domain.model.IrpfBracket
import com.sleeplessdog.banquerito.domain.model.IrpfBracketAmount
import com.sleeplessdog.banquerito.domain.model.IrpfBreakdown

object IrpfCalculator {

    fun calculate(
        quarterIncome: Double,
        yearToDateIncome: Double,
        currentMonth: Int, // 1..12 — текущий календарный месяц
        mode: IncomeExtrapolationMode,
        brackets: List<IrpfBracket>,
    ): IrpfBreakdown {
        val annualIncomeUsed = when (mode) {
            IncomeExtrapolationMode.QUARTER_ONLY -> quarterIncome
            IncomeExtrapolationMode.EXTRAPOLATE_CURRENT_QUARTER -> quarterIncome * 4
            IncomeExtrapolationMode.EXTRAPOLATE_FROM_PREVIOUS -> yearToDateIncome
        }

        if (annualIncomeUsed <= 0 || brackets.isEmpty()) {
            val firstBracket = brackets.firstOrNull() ?: IrpfBracket(0.0, 0.0, 0.0)
            return IrpfBreakdown(
                annualIncomeUsed = 0.0,
                totalTax = 0.0,
                effectiveRate = 0f,
                bracketAmounts = emptyList(),
                currentBracket = firstBracket,
                amountToNextBracketMonthly = null,
            )
        }

        val bracketAmounts = mutableListOf<IrpfBracketAmount>()
        var totalTax = 0.0
        var currentBracket = brackets.first()

        for (bracket in brackets) {
            if (annualIncomeUsed > bracket.from) {
                val taxableInBracket = minOf(annualIncomeUsed, bracket.to) - bracket.from
                val taxInBracket = taxableInBracket * bracket.rate
                totalTax += taxInBracket
                bracketAmounts.add(
                    IrpfBracketAmount(
                        bracket = bracket,
                        taxedAmount = taxableInBracket,
                        taxPaid = taxInBracket,
                    )
                )
                if (annualIncomeUsed > bracket.from && annualIncomeUsed <= bracket.to) {
                    currentBracket = bracket
                } else if (annualIncomeUsed > bracket.to) {
                    currentBracket = bracket
                }
            }
        }

        // текущий транш — тот в котором лежит annualIncomeUsed
        currentBracket = brackets.firstOrNull {
            annualIncomeUsed > it.from && annualIncomeUsed <= it.to
        } ?: brackets.last()

        val effectiveRate = if (annualIncomeUsed > 0) {
            (totalTax / annualIncomeUsed).toFloat()
        } else 0f

        val nextBracketIndex = brackets.indexOf(currentBracket) + 1
        val amountToNextBracketMonthly = if (nextBracketIndex < brackets.size) {
            val nextThreshold = currentBracket.to
            val remainingMonths = (12 - currentMonth + 1).coerceAtLeast(1)
            val remainingToThreshold = (nextThreshold - annualIncomeUsed).coerceAtLeast(0.0)
            remainingToThreshold / remainingMonths
        } else {
            null // уже в максимальном транше — дальше некуда
        }

        return IrpfBreakdown(
            annualIncomeUsed = annualIncomeUsed,
            totalTax = totalTax,
            effectiveRate = effectiveRate,
            bracketAmounts = bracketAmounts,
            currentBracket = currentBracket,
            amountToNextBracketMonthly = amountToNextBracketMonthly,
        )
    }
}
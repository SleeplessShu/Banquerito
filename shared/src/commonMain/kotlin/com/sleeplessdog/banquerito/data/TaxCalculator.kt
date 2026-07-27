package com.sleeplessdog.banquerito.data

import com.sleeplessdog.banquerito.domain.model.*
import kotlinx.datetime.LocalDate

object TaxCalculator {

    fun calculate(
        grossIncome: Double,
        currency: Currency,
        taxProfile: TaxProfile,
        monthsInPeriod: Int = 3, // квартал по умолчанию
    ): TaxCalculation {
        val settings = taxProfile.countryTaxSettings

        return when (settings) {
            is CountryTaxSettings.Spain -> calculateSpain(grossIncome, currency, settings, monthsInPeriod)
            is CountryTaxSettings.Serbia -> calculateSerbia(grossIncome, currency, settings, monthsInPeriod)
            is CountryTaxSettings.Armenia -> calculateArmenia(grossIncome, currency, settings)
            is CountryTaxSettings.None -> TaxCalculation(
                grossIncome = grossIncome,
                segments = listOf(
                    TaxSegment("Чистый доход", grossIncome, 1f, TaxSegmentColor.NET_INCOME)
                ),
                netIncome = grossIncome,
                currency = currency,
            )
        }
    }

    private fun calculateSpain(
        grossIncome: Double,
        currency: Currency,
        settings: CountryTaxSettings.Spain,
        monthsInPeriod: Int,
    ): TaxCalculation {
        if (settings.status != SpainEmploymentStatus.AUTONOMO || grossIncome <= 0) {
            return TaxCalculation(
                grossIncome = grossIncome,
                segments = listOf(
                    TaxSegment("Чистый доход", grossIncome, 1f, TaxSegmentColor.NET_INCOME)
                ),
                netIncome = grossIncome,
                currency = currency,
            )
        }

        val cuotaMonthly = when (settings.autonomoRegime) {
            SpainAutonomoRegime.TARIFA_PLANA -> TaxRates.SPAIN_TARIFA_PLANA_MONTHLY
            SpainAutonomoRegime.GENERAL -> TaxRates.SPAIN_GENERAL_CUOTA_MONTHLY
        }
        val cuota = cuotaMonthly * monthsInPeriod

        val irpf = grossIncome * TaxRates.SPAIN_IRPF_RESERVE_PERCENT
        val iva = if (settings.isIvaPayer) grossIncome * TaxRates.SPAIN_IVA_PERCENT else 0.0

        val netIncome = (grossIncome - cuota - irpf - iva).coerceAtLeast(0.0)

        val segments = buildList {
            add(TaxSegment("Чистый доход", netIncome, ratio(netIncome, grossIncome), TaxSegmentColor.NET_INCOME))
            add(TaxSegment("IRPF резерв", irpf, ratio(irpf, grossIncome), TaxSegmentColor.IRPF))
            if (iva > 0) add(TaxSegment("IVA", iva, ratio(iva, grossIncome), TaxSegmentColor.IVA))
            add(TaxSegment("Cuota autónomo", cuota, ratio(cuota, grossIncome), TaxSegmentColor.CUOTA))
        }

        return TaxCalculation(grossIncome, segments, netIncome, currency)
    }

    private fun calculateSerbia(
        grossIncome: Double,
        currency: Currency,
        settings: CountryTaxSettings.Serbia,
        monthsInPeriod: Int,
    ): TaxCalculation {
        if (settings.status != SerbiaEmploymentStatus.SOLE_TRADER || grossIncome <= 0) {
            return TaxCalculation(
                grossIncome = grossIncome,
                segments = listOf(
                    TaxSegment("Чистый доход", grossIncome, 1f, TaxSegmentColor.NET_INCOME)
                ),
                netIncome = grossIncome,
                currency = currency,
            )
        }

        val paushal = if (settings.pausalniPorez) {
            TaxRates.SERBIA_PAUSHAL_MONTHLY * monthsInPeriod
        } else 0.0
        val vat = if (settings.vatPayer) grossIncome * TaxRates.SERBIA_VAT_PERCENT else 0.0

        val netIncome = (grossIncome - paushal - vat).coerceAtLeast(0.0)

        val segments = buildList {
            add(TaxSegment("Чистый доход", netIncome, ratio(netIncome, grossIncome), TaxSegmentColor.NET_INCOME))
            if (vat > 0) add(TaxSegment("НДС", vat, ratio(vat, grossIncome), TaxSegmentColor.IVA))
            if (paushal > 0) add(TaxSegment("Паушальный налог", paushal, ratio(paushal, grossIncome), TaxSegmentColor.CUOTA))
        }

        return TaxCalculation(grossIncome, segments, netIncome, currency)
    }

    private fun calculateArmenia(
        grossIncome: Double,
        currency: Currency,
        settings: CountryTaxSettings.Armenia,
    ): TaxCalculation {
        if (grossIncome <= 0) {
            return TaxCalculation(
                grossIncome = grossIncome,
                segments = listOf(
                    TaxSegment("Чистый доход", grossIncome, 1f, TaxSegmentColor.NET_INCOME)
                ),
                netIncome = grossIncome,
                currency = currency,
            )
        }

        val profitTaxRate = if (settings.itZone) {
            TaxRates.ARMENIA_IT_ZONE_PROFIT_TAX_PERCENT
        } else {
            TaxRates.ARMENIA_GENERAL_PROFIT_TAX_PERCENT
        }
        val profitTax = grossIncome * profitTaxRate
        val vat = if (settings.vatPayer) grossIncome * TaxRates.ARMENIA_VAT_PERCENT else 0.0

        val netIncome = (grossIncome - profitTax - vat).coerceAtLeast(0.0)

        val segments = buildList {
            add(TaxSegment("Чистый доход", netIncome, ratio(netIncome, grossIncome), TaxSegmentColor.NET_INCOME))
            if (vat > 0) add(TaxSegment("НДС", vat, ratio(vat, grossIncome), TaxSegmentColor.IVA))
            add(TaxSegment("Налог на прибыль", profitTax, ratio(profitTax, grossIncome), TaxSegmentColor.IRPF))
        }

        return TaxCalculation(grossIncome, segments, netIncome, currency)
    }

    private fun ratio(part: Double, whole: Double): Float =
        if (whole <= 0) 0f else (part / whole).toFloat().coerceIn(0f, 1f)

    /**
     * Ближайшие дедлайны в зависимости от профиля.
     */
    fun getUpcomingDeadlines(
        taxProfile: TaxProfile,
        today: LocalDate,
    ): List<TaxDeadline> {
        val settings = taxProfile.countryTaxSettings
        val deadlines = mutableListOf<TaxDeadline>()

        if (settings is CountryTaxSettings.Spain && settings.status == SpainEmploymentStatus.AUTONOMO) {
            deadlines += nextQuarterlyDeadline(today, "Modelo 130/131")
            deadlines += nextAnnualDeadline(today, month = 6, day = 30, label = "Declaración de la Renta")
        }

        return deadlines.sortedBy { it.date }
    }

    private fun nextQuarterlyDeadline(today: LocalDate, label: String): TaxDeadline {
        // Испанские кварталы подаются 20 числа: апрель, июль, октябрь, январь
        val deadlineDays = listOf(1 to 20, 4 to 20, 7 to 20, 10 to 20)
        val candidates = deadlineDays.map { (month, day) ->
            LocalDate(today.year, month, day)
        }
        val futureDates = candidates.filter { it >= today }
        val next = futureDates.minByOrNull { it.toEpochDays() }
            ?: LocalDate(today.year + 1, 1, 20)
        return TaxDeadline(label, next)
    }

    private fun nextAnnualDeadline(today: LocalDate, month: Int, day: Int, label: String): TaxDeadline {
        var date = LocalDate(today.year, month, day)
        if (date < today) {
            date = LocalDate(today.year + 1, month, day)
        }
        return TaxDeadline(label, date)
    }
}
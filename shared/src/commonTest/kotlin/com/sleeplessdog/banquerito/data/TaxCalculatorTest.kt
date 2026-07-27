package com.sleeplessdog.banquerito.data

import com.sleeplessdog.banquerito.domain.model.*
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaxCalculatorTest {

    // ===================== ИСПАНИЯ =====================

    @Test
    fun `spain autonomo tarifa plana without iva calculates correctly`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(
                status = SpainEmploymentStatus.AUTONOMO,
                autonomoRegime = SpainAutonomoRegime.TARIFA_PLANA,
                isIvaPayer = false,
            )
        )

        val result = TaxCalculator.calculate(
            grossIncome = 6000.0,
            currency = Currency.EUR,
            taxProfile = profile,
            monthsInPeriod = 3,
        )

        val expectedCuota = TaxRates.SPAIN_TARIFA_PLANA_MONTHLY * 3
        val expectedIrpf = 6000.0 * TaxRates.SPAIN_IRPF_RESERVE_PERCENT
        val expectedNet = 6000.0 - expectedCuota - expectedIrpf

        assertEquals(6000.0, result.grossIncome)
        assertEquals(expectedNet, result.netIncome, 0.01)

        // IVA не должен присутствовать среди сегментов
        assertTrue(result.segments.none { it.label == "IVA" })

        val cuotaSegment = result.segments.first { it.label == "Cuota autónomo" }
        assertEquals(expectedCuota, cuotaSegment.amount, 0.01)

        val irpfSegment = result.segments.first { it.label == "IRPF резерв" }
        assertEquals(expectedIrpf, irpfSegment.amount, 0.01)
    }

    @Test
    fun `spain autonomo general with iva calculates correctly`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(
                status = SpainEmploymentStatus.AUTONOMO,
                autonomoRegime = SpainAutonomoRegime.GENERAL,
                isIvaPayer = true,
            )
        )

        val result = TaxCalculator.calculate(
            grossIncome = 10000.0,
            currency = Currency.EUR,
            taxProfile = profile,
            monthsInPeriod = 3,
        )

        val expectedCuota = TaxRates.SPAIN_GENERAL_CUOTA_MONTHLY * 3
        val expectedIrpf = 10000.0 * TaxRates.SPAIN_IRPF_RESERVE_PERCENT
        val expectedIva = 10000.0 * TaxRates.SPAIN_IVA_PERCENT
        val expectedNet = 10000.0 - expectedCuota - expectedIrpf - expectedIva

        assertEquals(expectedNet, result.netIncome, 0.01)
        assertTrue(result.segments.any { it.label == "IVA" })

        val ivaSegment = result.segments.first { it.label == "IVA" }
        assertEquals(expectedIva, ivaSegment.amount, 0.01)
    }

    @Test
    fun `spain non-autonomo status returns income unchanged`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(
                status = SpainEmploymentStatus.EMPLOYEE,
            )
        )

        val result = TaxCalculator.calculate(
            grossIncome = 5000.0,
            currency = Currency.EUR,
            taxProfile = profile,
        )

        assertEquals(5000.0, result.netIncome)
        assertEquals(1, result.segments.size)
        assertEquals("Чистый доход", result.segments.first().label)
        assertEquals(1f, result.segments.first().percentOfGross)
    }

    @Test
    fun `spain zero income returns zero net without crashing`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(
                status = SpainEmploymentStatus.AUTONOMO,
            )
        )

        val result = TaxCalculator.calculate(
            grossIncome = 0.0,
            currency = Currency.EUR,
            taxProfile = profile,
        )

        assertEquals(0.0, result.grossIncome)
        assertEquals(0.0, result.netIncome)
    }

    @Test
    fun `spain net income never goes negative when taxes exceed income`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(
                status = SpainEmploymentStatus.AUTONOMO,
                autonomoRegime = SpainAutonomoRegime.GENERAL,
                isIvaPayer = true,
            )
        )

        // очень маленький доход, cuota фиксированная — может превысить доход
        val result = TaxCalculator.calculate(
            grossIncome = 100.0,
            currency = Currency.EUR,
            taxProfile = profile,
            monthsInPeriod = 3,
        )

        assertTrue(result.netIncome >= 0.0)
    }

    @Test
    fun `spain segments percent sums roughly to expected proportions`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(
                status = SpainEmploymentStatus.AUTONOMO,
                autonomoRegime = SpainAutonomoRegime.TARIFA_PLANA,
                isIvaPayer = false,
            )
        )

        val result = TaxCalculator.calculate(
            grossIncome = 5000.0,
            currency = Currency.EUR,
            taxProfile = profile,
            monthsInPeriod = 3,
        )

        result.segments.forEach { segment ->
            val expectedPercent = (segment.amount / result.grossIncome).toFloat()
            assertEquals(expectedPercent, segment.percentOfGross, 0.01f)
        }
    }

    // ===================== СЕРБИЯ =====================

    @Test
    fun `serbia sole trader with paushal and vat`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SERBIA,
            countryTaxSettings = CountryTaxSettings.Serbia(
                status = SerbiaEmploymentStatus.SOLE_TRADER,
                pausalniPorez = true,
                vatPayer = true,
            )
        )

        val result = TaxCalculator.calculate(
            grossIncome = 3000.0,
            currency = Currency.EUR,
            taxProfile = profile,
            monthsInPeriod = 3,
        )

        val expectedPaushal = TaxRates.SERBIA_PAUSHAL_MONTHLY * 3
        val expectedVat = 3000.0 * TaxRates.SERBIA_VAT_PERCENT
        val expectedNet = (3000.0 - expectedPaushal - expectedVat).coerceAtLeast(0.0)

        assertEquals(expectedNet, result.netIncome, 0.01)
        assertTrue(result.segments.any { it.label == "Паушальный налог" })
        assertTrue(result.segments.any { it.label == "НДС" })
    }

    @Test
    fun `serbia sole trader without paushal excludes that segment`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SERBIA,
            countryTaxSettings = CountryTaxSettings.Serbia(
                status = SerbiaEmploymentStatus.SOLE_TRADER,
                pausalniPorez = false,
                vatPayer = false,
            )
        )

        val result = TaxCalculator.calculate(
            grossIncome = 2000.0,
            currency = Currency.EUR,
            taxProfile = profile,
        )

        assertEquals(2000.0, result.netIncome)
        assertTrue(result.segments.none { it.label == "Паушальный налог" })
        assertTrue(result.segments.none { it.label == "НДС" })
    }

    @Test
    fun `serbia non-sole-trader status returns income unchanged`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SERBIA,
            countryTaxSettings = CountryTaxSettings.Serbia(
                status = SerbiaEmploymentStatus.EMPLOYEE,
            )
        )

        val result = TaxCalculator.calculate(
            grossIncome = 1500.0,
            currency = Currency.EUR,
            taxProfile = profile,
        )

        assertEquals(1500.0, result.netIncome)
        assertEquals(1, result.segments.size)
    }

    // ===================== АРМЕНИЯ =====================

    @Test
    fun `armenia it zone applies reduced profit tax`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.ARMENIA,
            countryTaxSettings = CountryTaxSettings.Armenia(
                itZone = true,
                vatPayer = false,
            )
        )

        val result = TaxCalculator.calculate(
            grossIncome = 4000.0,
            currency = Currency.EUR,
            taxProfile = profile,
        )

        val expectedTax = 4000.0 * TaxRates.ARMENIA_IT_ZONE_PROFIT_TAX_PERCENT
        val expectedNet = 4000.0 - expectedTax

        assertEquals(expectedNet, result.netIncome, 0.01)
    }

    @Test
    fun `armenia general regime applies higher profit tax than it zone`() {
        val itProfile = TaxProfile(
            countryTaxSettings = CountryTaxSettings.Armenia(itZone = true, vatPayer = false)
        )
        val generalProfile = TaxProfile(
            countryTaxSettings = CountryTaxSettings.Armenia(itZone = false, vatPayer = false)
        )

        val itResult = TaxCalculator.calculate(4000.0, Currency.EUR, itProfile)
        val generalResult = TaxCalculator.calculate(4000.0, Currency.EUR, generalProfile)

        assertTrue(itResult.netIncome > generalResult.netIncome)
    }

    @Test
    fun `armenia with vat payer includes vat segment`() {
        val profile = TaxProfile(
            countryTaxSettings = CountryTaxSettings.Armenia(itZone = true, vatPayer = true)
        )

        val result = TaxCalculator.calculate(4000.0, Currency.EUR, profile)

        assertTrue(result.segments.any { it.label == "НДС" })
    }

    @Test
    fun `armenia zero income returns net income unchanged as single segment`() {
        val profile = TaxProfile(
            countryTaxSettings = CountryTaxSettings.Armenia(itZone = true, vatPayer = true)
        )

        val result = TaxCalculator.calculate(0.0, Currency.EUR, profile)

        assertEquals(0.0, result.netIncome)
        assertEquals(1, result.segments.size)
    }

    // ===================== НЕТ НАЛОГОВЫХ НАСТРОЕК =====================

    @Test
    fun `none country settings returns full income as net`() {
        val profile = TaxProfile(countryTaxSettings = CountryTaxSettings.None)

        val result = TaxCalculator.calculate(2500.0, Currency.EUR, profile)

        assertEquals(2500.0, result.netIncome)
        assertEquals(1, result.segments.size)
        assertEquals("Чистый доход", result.segments.first().label)
    }

    // ===================== ДЕДЛАЙНЫ =====================

    @Test
    fun `deadlines for spain autonomo include quarterly and renta`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(status = SpainEmploymentStatus.AUTONOMO)
        )
        val today = LocalDate(2026, 5, 1)

        val deadlines = TaxCalculator.getUpcomingDeadlines(profile, today)

        assertEquals(2, deadlines.size)
        assertTrue(deadlines.any { it.label == "Modelo 130/131" })
        assertTrue(deadlines.any { it.label == "Declaración de la Renta" })
    }

    @Test
    fun `deadlines are sorted chronologically`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(status = SpainEmploymentStatus.AUTONOMO)
        )
        val today = LocalDate(2026, 5, 1)

        val deadlines = TaxCalculator.getUpcomingDeadlines(profile, today)

        val sorted = deadlines.sortedBy { it.date.toEpochDays() }
        assertEquals(sorted.map { it.date }, deadlines.map { it.date })
    }

    @Test
    fun `deadlines are always in the future relative to today`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(status = SpainEmploymentStatus.AUTONOMO)
        )
        val today = LocalDate(2026, 7, 15)

        val deadlines = TaxCalculator.getUpcomingDeadlines(profile, today)

        deadlines.forEach { deadline ->
            assertTrue(
                deadline.date.toEpochDays() >= today.toEpochDays(),
                "Deadline ${deadline.label} on ${deadline.date} should not be in the past"
            )
        }
    }

    @Test
    fun `deadlines empty for non-autonomo spain status`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(status = SpainEmploymentStatus.EMPLOYEE)
        )
        val today = LocalDate(2026, 5, 1)

        val deadlines = TaxCalculator.getUpcomingDeadlines(profile, today)

        assertTrue(deadlines.isEmpty())
    }

    @Test
    fun `deadlines empty for serbia and armenia (not yet implemented)`() {
        val serbiaProfile = TaxProfile(
            taxResidency = TaxResidency.SERBIA,
            countryTaxSettings = CountryTaxSettings.Serbia(status = SerbiaEmploymentStatus.SOLE_TRADER)
        )
        val armeniaProfile = TaxProfile(
            taxResidency = TaxResidency.ARMENIA,
            countryTaxSettings = CountryTaxSettings.Armenia()
        )
        val today = LocalDate(2026, 5, 1)

        assertTrue(TaxCalculator.getUpcomingDeadlines(serbiaProfile, today).isEmpty())
        assertTrue(TaxCalculator.getUpcomingDeadlines(armeniaProfile, today).isEmpty())
    }

    @Test
    fun `quarterly deadline right after cutoff jumps to next quarter`() {
        // 21 апреля — сразу после дедлайна 20 апреля, следующий должен быть 20 июля
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(status = SpainEmploymentStatus.AUTONOMO)
        )
        val today = LocalDate(2026, 4, 21)

        val deadlines = TaxCalculator.getUpcomingDeadlines(profile, today)
        val quarterly = deadlines.first { it.label == "Modelo 130/131" }

        assertEquals(LocalDate(2026, 7, 20), quarterly.date)
    }

    @Test
    fun `quarterly deadline wraps to january next year after october`() {
        val profile = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(status = SpainEmploymentStatus.AUTONOMO)
        )
        val today = LocalDate(2026, 11, 1)

        val deadlines = TaxCalculator.getUpcomingDeadlines(profile, today)
        val quarterly = deadlines.first { it.label == "Modelo 130/131" }

        assertEquals(LocalDate(2027, 1, 20), quarterly.date)
    }
}
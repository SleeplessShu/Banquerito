package com.sleeplessdog.banquerito.domain.model

/**
 * Актуальные ставки на 2026 год. Хардкод констант
 */
object TaxRates {

    // === Испания — cuota autónomo (месячная, EUR) ===
    const val SPAIN_TARIFA_PLANA_MONTHLY = 80.0
    const val SPAIN_GENERAL_CUOTA_MONTHLY = 294.0

    // === Испания — IRPF ===
    const val SPAIN_IRPF_RESERVE_PERCENT = 0.20 // консервативный резерв 20%

    // === Испания — IVA ===
    const val SPAIN_IVA_PERCENT = 0.21

    // === Сербия — паушальный налог (примерная месячная сумма EUR) ===
    const val SERBIA_PAUSHAL_MONTHLY = 250.0
    const val SERBIA_VAT_PERCENT = 0.20

    // === Армения — IT zone ===
    const val ARMENIA_IT_ZONE_PROFIT_TAX_PERCENT = 0.10
    const val ARMENIA_GENERAL_PROFIT_TAX_PERCENT = 0.18
    const val ARMENIA_VAT_PERCENT = 0.20
}
package com.sleeplessdog.banquerito.data.repository

import com.sleeplessdog.banquerito.data.interfaces.IExchangeRateRepository
import com.sleeplessdog.banquerito.data.remote.ExchangeRateApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExchangeRateRepository(private val api: ExchangeRateApi): IExchangeRateRepository {

    private val _rates = MutableStateFlow<Map<String, Double>>(
        // дефолтные значения пока не загрузились
        mapOf(
            "EUR" to 1.0,
            "USD" to 1.08,
            "GBP" to 0.85,
            "RUB" to 98.0,
            "GEL" to 2.85,
            "AMD" to 386.0,
            "TRY" to 35.0,
            "ILS" to 3.9,
            "UAH" to 42.0,
            "BYN" to 3.5,
            "KZT" to 480.0,
            "UZS" to 13500.0,
            "AZN" to 1.83,
            "PLN" to 4.25,
            "CZK" to 25.0,
            "HUF" to 395.0,
            "RON" to 4.97,
            "BGN" to 1.96,
            "HRK" to 7.53,
            "RSD" to 117.0,
            "NOK" to 11.5,
            "SEK" to 11.3,
            "DKK" to 7.46,
            "CHF" to 0.94,
        )
    )
    override val rates: StateFlow<Map<String, Double>> = _rates.asStateFlow()

    override suspend fun refresh(baseCurrency: String) {
        val newRates = api.getRates(baseCurrency)
        if (newRates.isNotEmpty()) {
            _rates.value = newRates
        }
    }

    override fun convert(amount: Double, from: String, to: String): Double {
        val r = _rates.value
        if (from == to) return amount
        val inBase = amount / (r[from] ?: 1.0)
        return inBase * (r[to] ?: 1.0)
    }
}
package com.sleeplessdog.banquerito.testutil

import com.sleeplessdog.banquerito.data.interfaces.IExchangeRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
class FakeExchangeRateRepository : IExchangeRateRepository {

    private val _rates = MutableStateFlow(
        mapOf("EUR" to 1.0, "USD" to 1.08, "GBP" to 0.85, "RUB" to 98.0)
    )
    override val rates: StateFlow<Map<String, Double>> = _rates.asStateFlow()

    var refreshCalled = false

    override suspend fun refresh(baseCurrency: String) {
        refreshCalled = true
    }

    override fun convert(amount: Double, from: String, to: String): Double {
        if (from == to) return amount
        val r = _rates.value
        val inBase = amount / (r[from] ?: 1.0)
        return inBase * (r[to] ?: 1.0)
    }

    fun setRates(newRates: Map<String, Double>) {
        _rates.value = newRates
    }
}
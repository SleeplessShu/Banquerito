package com.sleeplessdog.banquerito.data.interfaces

import kotlinx.coroutines.flow.StateFlow

interface IExchangeRateRepository {

    val rates: StateFlow<Map<String, Double>>

    suspend fun refresh(baseCurrency: String = "EUR")

    fun convert(amount: Double, from: String, to: String): Double
}
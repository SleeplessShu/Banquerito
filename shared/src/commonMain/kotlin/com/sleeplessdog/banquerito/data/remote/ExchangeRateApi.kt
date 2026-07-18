package com.sleeplessdog.banquerito.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive

class ExchangeRateApi {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getRates(baseCurrency: String = "eur"): Map<String, Double> {
        return try {
            val base = baseCurrency.lowercase()
            val response: JsonObject = client.get(
                "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/$base.json"
            ).body()
            val ratesObject = response[base] as? JsonObject ?: return emptyMap()
            ratesObject.entries.associate { (key, value) ->
                key.uppercase() to value.jsonPrimitive.double
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
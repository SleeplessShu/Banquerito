package com.sleeplessdog.banquerito.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class TaxProfile(
    val taxResidency: String = "",
    val employmentStatus: EmploymentStatus = EmploymentStatus.EMPLOYEE,
    val countryTaxSettings: CountryTaxSettings = CountryTaxSettings.None,
    val remindQuarterlyDays: Int = 7,
    val remindRentaDays: Int = 14,
)

enum class EmploymentStatus(val label: String) {
    EMPLOYEE("Наёмный сотрудник"),
    AUTONOMO("Autónomo"),
    NON_RESIDENT("Нерезидент"),
    OTHER("Другое"),
}

@Serializable
sealed class CountryTaxSettings {

    @Serializable
    data object None : CountryTaxSettings()

    @Serializable
    data class Spain(
        val autonomoRegime: AutonomoRegime = AutonomoRegime.GENERAL,
        val autonomoStartYear: Int? = null,
        val epigrafe: String = "",
        val isIvaPayer: Boolean = true,
        val declarationType: DeclarationType = DeclarationType.MODELO_130,
    ) : CountryTaxSettings()

    @Serializable
    data class Serbia(
        val pausalniPorez: Boolean = true,
        val vatPayer: Boolean = false,
    ) : CountryTaxSettings()

    @Serializable
    data class Armenia(
        val itZone: Boolean = false,
        val vatPayer: Boolean = false,
    ) : CountryTaxSettings()
}

enum class AutonomoRegime(val label: String) {
    TARIFA_PLANA("Tarifa plana"),
    GENERAL("Régimen general"),
}

enum class DeclarationType(val label: String) {
    MODELO_130("Modelo 130"),
    MODELO_131("Modelo 131"),
}

val taxSettingsJson = Json { ignoreUnknownKeys = true }

fun CountryTaxSettings.toJson(): String =
    taxSettingsJson.encodeToString(CountryTaxSettings.serializer(), this)

fun String.toCountryTaxSettings(): CountryTaxSettings =
    try {
        taxSettingsJson.decodeFromString(CountryTaxSettings.serializer(), this)
    } catch (e: Exception) {
        CountryTaxSettings.None
    }
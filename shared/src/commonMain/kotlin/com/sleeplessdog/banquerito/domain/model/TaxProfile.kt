package com.sleeplessdog.banquerito.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class TaxProfile(
    val taxResidency: TaxResidency = TaxResidency.SPAIN,
    val countryTaxSettings: CountryTaxSettings = CountryTaxSettings.None,
    val remindQuarterlyDays: Int = 7,
    val remindRentaDays: Int = 14,
)

enum class TaxResidency(val label: String) {
    SPAIN("Испания"),
    SERBIA("Сербия"),
    ARMENIA("Армения"),
    RUSSIA("Россия"),
    OTHER("Другое"),
}

@Serializable
sealed class CountryTaxSettings {

    @Serializable
    data object None : CountryTaxSettings()

    @Serializable
    data class Spain(
        val status: SpainEmploymentStatus = SpainEmploymentStatus.AUTONOMO,
        val autonomoRegime: SpainAutonomoRegime = SpainAutonomoRegime.GENERAL,
        val autonomoStartYear: Int? = null,
        val epigrafe: String = "",
        val isIvaPayer: Boolean = true,
        val declarationType: SpainDeclarationType = SpainDeclarationType.MODELO_130,
        val visaExpiryDate: String? = null,
        val tieExpiryDate: String? = null,
        val remindVisaDays: Int = 30,
        val partnerVisaExpiryDate: String? = null,
    ) : CountryTaxSettings()

    @Serializable
    data class Serbia(
        val status: SerbiaEmploymentStatus = SerbiaEmploymentStatus.SOLE_TRADER,
        val pausalniPorez: Boolean = true,
        val vatPayer: Boolean = false,
        val visaExpiryDate: String? = null,
        val remindVisaDays: Int = 30,
    ) : CountryTaxSettings()

    @Serializable
    data class Armenia(
        val status: ArmeniaEmploymentStatus = ArmeniaEmploymentStatus.INDIVIDUAL_ENTREPRENEUR,
        val itZone: Boolean = false,
        val vatPayer: Boolean = false,
    ) : CountryTaxSettings()
}

enum class SpainEmploymentStatus(val label: String) {
    AUTONOMO("Autónomo"),
    EMPLOYEE("Наёмный сотрудник"),
    NON_RESIDENT("Нерезидент"),
    DIGITAL_NOMAD("Digital Nomad Visa"),
    DEPENDENT_AUTONOMO("Зависимый · партнёр Autónomo"),
    DEPENDENT_NOMAD("Зависимый · партнёр Digital Nomad"),
}

enum class SerbiaEmploymentStatus(val label: String) {
    SOLE_TRADER("Preduzetnik (паушал)"),
    DOO("D.O.O."),
    EMPLOYEE("Наёмный сотрудник"),
    FREELANCE_FOREIGN("Иностранный фрилансер"),
}

enum class ArmeniaEmploymentStatus(val label: String) {
    INDIVIDUAL_ENTREPRENEUR("Индивидуальный предприниматель"),
    IT_COMPANY("IT компания"),
    EMPLOYEE("Наёмный сотрудник"),
    FOREIGN_FREELANCER("Иностранный фрилансер"),
}

enum class SpainAutonomoRegime(val label: String) {
    TARIFA_PLANA("Tarifa plana"),
    GENERAL("Régimen general"),
}

enum class SpainDeclarationType(val label: String) {
    MODELO_130("Modelo 130"),
    MODELO_131("Modelo 131"),
}

val taxSettingsJson = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
}

fun CountryTaxSettings.toJson(): String =
    taxSettingsJson.encodeToString(CountryTaxSettings.serializer(), this)

fun String.toCountryTaxSettings(): CountryTaxSettings =
    try {
        taxSettingsJson.decodeFromString(CountryTaxSettings.serializer(), this)
    } catch (e: Exception) {
        CountryTaxSettings.None
    }
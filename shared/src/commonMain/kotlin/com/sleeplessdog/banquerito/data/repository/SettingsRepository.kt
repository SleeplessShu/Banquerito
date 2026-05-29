package com.sleeplessdog.banquerito.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.sleeplessdog.banquerito.db.BanqueritoDB
import com.sleeplessdog.banquerito.domain.model.Citizenship
import com.sleeplessdog.banquerito.domain.model.CountryOfResidence
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.TaxProfile
import com.sleeplessdog.banquerito.domain.model.TaxResidency
import com.sleeplessdog.banquerito.domain.model.UserProfile
import com.sleeplessdog.banquerito.domain.model.toCountryTaxSettings
import com.sleeplessdog.banquerito.domain.model.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val db: BanqueritoDB) {

    fun getUserProfile(): Flow<UserProfile> =
        db.banqueritoDBQueries.selectUserProfile()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toUserProfile() ?: UserProfile() }

    fun getTaxProfile(): Flow<TaxProfile> =
        db.banqueritoDBQueries.selectTaxProfile()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toTaxProfile() ?: TaxProfile() }

    suspend fun upsertUserProfile(profile: UserProfile) {
        db.banqueritoDBQueries.upsertUserProfile(
            name = profile.name,
            country_of_residence = profile.countryOfResidence.name,
            citizenship = profile.citizenship.name,
            default_currency = profile.defaultCurrency.code
        )
    }

    suspend fun upsertTaxProfile(profile: TaxProfile) {
        db.banqueritoDBQueries.upsertTaxProfile(
            tax_residency = profile.taxResidency.name,
            country_tax_settings_json = profile.countryTaxSettings.toJson(),
            remind_quarterly_days = profile.remindQuarterlyDays.toLong(),
            remind_renta_days = profile.remindRentaDays.toLong(),
        )
    }
}

private fun com.sleeplessdog.banquerito.db.UserProfile.toUserProfile() = UserProfile(
    name = name,
    countryOfResidence = CountryOfResidence.valueOf(country_of_residence),
    citizenship = Citizenship.valueOf(citizenship),
    defaultCurrency = Currency.entries.find { it.code == default_currency } ?: Currency.EUR
)

private fun com.sleeplessdog.banquerito.db.TaxProfile.toTaxProfile() = TaxProfile(
    taxResidency = TaxResidency.valueOf(tax_residency),
    countryTaxSettings = country_tax_settings_json.toCountryTaxSettings(),
    remindQuarterlyDays = remind_quarterly_days.toInt(),
    remindRentaDays = remind_renta_days.toInt()
)
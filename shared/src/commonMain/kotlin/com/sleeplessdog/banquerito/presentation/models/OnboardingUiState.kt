package com.sleeplessdog.banquerito.presentation.models

import com.sleeplessdog.banquerito.domain.model.Citizenship
import com.sleeplessdog.banquerito.domain.model.Country
import com.sleeplessdog.banquerito.domain.model.CountryOfResidence
import com.sleeplessdog.banquerito.domain.model.CountryTaxSettings
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.OnboardingSteps
import com.sleeplessdog.banquerito.domain.model.SerbiaEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.SpainEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.TaxResidency

data class OnboardingUiState(
    val step: OnboardingSteps = OnboardingSteps.WELCOME,
    val selectedResidenceCountry: Country? = null,
    val selectedCitizenshipCountry: Country? = null,
    val selectedTaxResidencyCountry: Country? = null,
    val countryOfResidence: CountryOfResidence? = null,
    val citizenship: Citizenship? = null,
    val defaultCurrency: Currency = Currency.EUR,
    val taxResidency: TaxResidency? = null,
    val countryTaxSettings: CountryTaxSettings = CountryTaxSettings.Spain(),
    val remindQuarterlyDays: Int = 7,
    val remindRentaDays: Int = 14,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
    val visaReminderEnabled: Boolean = true,
    val quarterlyReminderEnabled: Boolean = true,
) {
    val isCurrentStepValid: Boolean get() = when (step) {
        OnboardingSteps.COUNTRY_OF_RESIDENCE -> selectedResidenceCountry != null
        OnboardingSteps.CITIZENSHIP -> selectedCitizenshipCountry != null
        OnboardingSteps.TAX_RESIDENCY -> selectedTaxResidencyCountry != null
        OnboardingSteps.REMINDERS -> true
        else -> true
    }
    fun activeSteps(): List<OnboardingSteps> {
        val steps = mutableListOf(
            OnboardingSteps.WELCOME,
            OnboardingSteps.COUNTRY_OF_RESIDENCE,
            OnboardingSteps.CITIZENSHIP,
            OnboardingSteps.DEFAULT_CURRENCY,
            OnboardingSteps.TAX_RESIDENCY,

        )
        when (val settings = countryTaxSettings) {
            is CountryTaxSettings.Spain -> {
                steps += OnboardingSteps.ALMOST_DONE
                steps += OnboardingSteps.SPAIN_STATUS
                if (settings.status == SpainEmploymentStatus.AUTONOMO) {
                    steps += OnboardingSteps.SPAIN_REGIME
                    steps += OnboardingSteps.SPAIN_START_YEAR
                    steps += OnboardingSteps.SPAIN_IAE
                    steps += OnboardingSteps.SPAIN_IVA_PAYER
                    steps += OnboardingSteps.SPAIN_DECLARATION_TYPE
                }
                if (settings.status != SpainEmploymentStatus.EMPLOYEE) {
                    steps += OnboardingSteps.SPAIN_VISA_EXPIRY
                    steps += OnboardingSteps.SPAIN_TIE_EXPIRY
                }
                if (settings.status == SpainEmploymentStatus.AUTONOMO) {
                    steps += OnboardingSteps.REMINDERS
                }
            }
            is CountryTaxSettings.Serbia -> {
                steps += OnboardingSteps.ALMOST_DONE
                steps += OnboardingSteps.SERBIA_STATUS
                if (settings.status == SerbiaEmploymentStatus.SOLE_TRADER) {
                    steps += OnboardingSteps.SERBIA_PAUSHALNI
                }
                if (settings.status == SerbiaEmploymentStatus.SOLE_TRADER ||
                    settings.status == SerbiaEmploymentStatus.DOO
                ) {
                    steps += OnboardingSteps.SERBIA_VAT
                }
                steps += OnboardingSteps.SERBIA_VISA_EXPIRY
                steps += OnboardingSteps.REMINDERS
            }
            is CountryTaxSettings.Armenia -> {
                steps += OnboardingSteps.ALMOST_DONE
                steps += OnboardingSteps.ARMENIA_STATUS
                steps += OnboardingSteps.ARMENIA_IT_ZONE
                steps += OnboardingSteps.ARMENIA_VAT
            }
            is CountryTaxSettings.None -> {}
        }
        return steps
    }
}
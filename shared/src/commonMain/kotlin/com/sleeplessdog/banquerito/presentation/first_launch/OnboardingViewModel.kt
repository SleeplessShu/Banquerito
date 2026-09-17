package com.sleeplessdog.banquerito.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplessdog.banquerito.data.interfaces.ISettingsRepository
import com.sleeplessdog.banquerito.domain.model.ArmeniaEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.Citizenship
import com.sleeplessdog.banquerito.domain.model.Country
import com.sleeplessdog.banquerito.domain.model.CountryOfResidence
import com.sleeplessdog.banquerito.domain.model.CountryTaxSettings
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.OnboardingSteps
import com.sleeplessdog.banquerito.domain.model.SerbiaEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.SpainAutonomoRegime
import com.sleeplessdog.banquerito.domain.model.SpainDeclarationType
import com.sleeplessdog.banquerito.domain.model.SpainEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.TaxProfile
import com.sleeplessdog.banquerito.domain.model.TaxResidency
import com.sleeplessdog.banquerito.domain.model.UserProfile
import com.sleeplessdog.banquerito.presentation.models.OnboardingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class OnboardingViewModel(
    private val repository: ISettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun next() {
        val state = _uiState.value
        val steps = state.activeSteps()
        println("next() called, step=${state.step}, activeSteps=$steps")
        val nextStep = steps.getOrNull(steps.indexOf(state.step) + 1)
        println("nextStep=$nextStep")
        if (nextStep != null) _uiState.update { it.copy(step = nextStep) }
        else finishOnboarding()
    }

    fun back(): Boolean {
        val state = _uiState.value
        val steps = state.activeSteps()
        val prevStep = steps.getOrNull(steps.indexOf(state.step) - 1) ?: return false
        _uiState.update { it.copy(step = prevStep) }
        return true
    }

    // ── Базовый профиль ───────────────────────────────────────────────────────
    fun selectCountryOfResidence(country: Country) {
        _uiState.update {
            it.copy(
                selectedResidenceCountry = country,
                countryOfResidence = country.asResidence ?: CountryOfResidence.OTHER,
            )
        }
    }

    fun selectCitizenship(country: Country) {
        _uiState.update {
            it.copy(
                selectedCitizenshipCountry = country,
                citizenship = country.asCitizenship ?: Citizenship.OTHER,
            )
        }
    }

    fun selectTaxResidency(country: Country) {
        val taxResidency = country.asTaxResidency ?: TaxResidency.OTHER
        val settings = when (taxResidency) {
            TaxResidency.SPAIN -> CountryTaxSettings.Spain()
            TaxResidency.SERBIA -> CountryTaxSettings.Serbia()
            TaxResidency.ARMENIA -> CountryTaxSettings.Armenia()
            else -> CountryTaxSettings.None
        }
        _uiState.update {
            it.copy(
                selectedTaxResidencyCountry = country,
                taxResidency = taxResidency,
                countryTaxSettings = settings,
            )
        }
    }

    fun selectCurrency(value: Currency) =
        _uiState.update { it.copy(defaultCurrency = value) }

    // ── Испания ───────────────────────────────────────────────────────────────

    fun updateSpainStatus(value: SpainEmploymentStatus) {
        updateSpain { copy(status = value) }
        // Если текущий шаг выпал из activeSteps — вернуться к выбору статуса
        val newState = _uiState.value
        if (newState.step !in newState.activeSteps()) {
            _uiState.update { it.copy(step = OnboardingSteps.SPAIN_STATUS) }
        }
    }

    fun updateSpainRegime(value: SpainAutonomoRegime) = updateSpain { copy(autonomoRegime = value) }
    fun updateSpainStartYear(value: String) = updateSpain { copy(autonomoStartYear = value.toIntOrNull()) }
    fun updateSpainIae(value: String) = updateSpain { copy(epigrafe = value) }
    fun updateSpainIvaPayer(value: Boolean) = updateSpain { copy(isIvaPayer = value) }
    fun updateSpainDeclarationType(value: SpainDeclarationType) = updateSpain { copy(declarationType = value) }
    fun updateSpainVisaExpiry(value: String) = updateSpain { copy(visaExpiryDate = value.ifBlank { null }) }
    fun updateSpainTieExpiry(value: String) = updateSpain { copy(tieExpiryDate = value.ifBlank { null }) }
    fun updateSpainRemindVisaDays(value: Int) = updateSpain { copy(remindVisaDays = value) }
    fun updateRemindQuarterlyDays(value: Int) = _uiState.update { it.copy(remindQuarterlyDays = value) }
    fun updateRemindRentaDays(value: Int) = _uiState.update { it.copy(remindRentaDays = value) }

    private fun updateSpain(block: CountryTaxSettings.Spain.() -> CountryTaxSettings.Spain) {
        val current = _uiState.value.countryTaxSettings
        if (current is CountryTaxSettings.Spain) {
            _uiState.update { it.copy(countryTaxSettings = current.block()) }
        }
    }

    fun toggleVisaReminder(enabled: Boolean) =
        _uiState.update { it.copy(visaReminderEnabled = enabled) }

    fun toggleQuarterlyReminder(enabled: Boolean) =
        _uiState.update { it.copy(quarterlyReminderEnabled = enabled) }

    // ── Сербия ────────────────────────────────────────────────────────────────

    fun updateSerbiaStatus(value: SerbiaEmploymentStatus) {
        updateSerbia { copy(status = value) }
        val newState = _uiState.value
        if (newState.step !in newState.activeSteps()) {
            _uiState.update { it.copy(step = OnboardingSteps.SERBIA_STATUS) }
        }
    }

    fun updateSerbiaPaushalni(value: Boolean) = updateSerbia { copy(pausalniPorez = value) }
    fun updateSerbiaVat(value: Boolean) = updateSerbia { copy(vatPayer = value) }
    fun updateSerbiaVisaExpiry(value: String) = updateSerbia { copy(visaExpiryDate = value.ifBlank { null }) }
    fun updateSerbiaRemindVisaDays(value: Int) = updateSerbia { copy(remindVisaDays = value) }

    private fun updateSerbia(block: CountryTaxSettings.Serbia.() -> CountryTaxSettings.Serbia) {
        val current = _uiState.value.countryTaxSettings
        if (current is CountryTaxSettings.Serbia) {
            _uiState.update { it.copy(countryTaxSettings = current.block()) }
        }
    }

    // ── Армения ───────────────────────────────────────────────────────────────

    fun updateArmeniaStatus(value: ArmeniaEmploymentStatus) = updateArmenia { copy(status = value) }
    fun updateArmeniaItZone(value: Boolean) = updateArmenia { copy(itZone = value) }
    fun updateArmeniaVat(value: Boolean) = updateArmenia { copy(vatPayer = value) }

    private fun updateArmenia(block: CountryTaxSettings.Armenia.() -> CountryTaxSettings.Armenia) {
        val current = _uiState.value.countryTaxSettings
        if (current is CountryTaxSettings.Armenia) {
            _uiState.update { it.copy(countryTaxSettings = current.block()) }
        }
    }

    // ── Финализация ───────────────────────────────────────────────────────────

    private fun finishOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            repository.upsertUserProfile(
                UserProfile(
                    countryOfResidence = state.countryOfResidence ?: CountryOfResidence.SPAIN,
                    citizenship = state.citizenship ?: Citizenship.RUSSIA,
                    defaultCurrency = state.defaultCurrency,
                )
            )
            repository.upsertTaxProfile(
                TaxProfile(
                    taxResidency = state.taxResidency ?: TaxResidency.SPAIN,
                    countryTaxSettings = state.countryTaxSettings,
                    remindQuarterlyDays = state.remindQuarterlyDays,
                    remindRentaDays = state.remindRentaDays,
                )
            )
            repository.setFirstLaunchComplete()
            _uiState.update { it.copy(isSaving = false, isComplete = true) }
        }
    }
}
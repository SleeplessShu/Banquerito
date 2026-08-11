package com.sleeplessdog.banquerito.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplessdog.banquerito.data.interfaces.ISettingsRepository
import com.sleeplessdog.banquerito.domain.model.ArmeniaEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.Citizenship
import com.sleeplessdog.banquerito.domain.model.CountryOfResidence
import com.sleeplessdog.banquerito.domain.model.CountryTaxSettings
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.SerbiaEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.SpainAutonomoRegime
import com.sleeplessdog.banquerito.domain.model.SpainDeclarationType
import com.sleeplessdog.banquerito.domain.model.SpainEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.TaxProfile
import com.sleeplessdog.banquerito.domain.model.TaxResidency
import com.sleeplessdog.banquerito.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
enum class OnboardingStep {
    WELCOME,
    COUNTRY_OF_RESIDENCE,
    CITIZENSHIP,
    DEFAULT_CURRENCY,
    TAX_RESIDENCY,
    ALMOST_DONE,
    // Испания
    SPAIN_STATUS,
    SPAIN_REGIME,
    SPAIN_START_YEAR,
    SPAIN_IAE,
    SPAIN_IVA_PAYER,
    SPAIN_DECLARATION_TYPE,
    SPAIN_VISA_EXPIRY,
    SPAIN_TIE_EXPIRY,
    SPAIN_REMIND_VISA,
    SPAIN_REMIND_QUARTERLY,
    SPAIN_REMIND_RENTA,
    // Сербия
    SERBIA_STATUS,
    SERBIA_PAUSHALNI,
    SERBIA_VAT,
    SERBIA_VISA_EXPIRY,
    SERBIA_REMIND_VISA,
    // Армения
    ARMENIA_STATUS,
    ARMENIA_IT_ZONE,
    ARMENIA_VAT,
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val countryOfResidence: CountryOfResidence = CountryOfResidence.SPAIN,
    val citizenship: Citizenship = Citizenship.OTHER,
    val defaultCurrency: Currency = Currency.EUR,
    val taxResidency: TaxResidency = TaxResidency.SPAIN,
    val countryTaxSettings: CountryTaxSettings = CountryTaxSettings.Spain(),
    val remindQuarterlyDays: Int = 7,
    val remindRentaDays: Int = 14,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
) {
    fun activeSteps(): List<OnboardingStep> {
        val steps = mutableListOf(
            OnboardingStep.WELCOME,
            OnboardingStep.COUNTRY_OF_RESIDENCE,
            OnboardingStep.CITIZENSHIP,
            OnboardingStep.DEFAULT_CURRENCY,
            OnboardingStep.TAX_RESIDENCY,
            OnboardingStep.ALMOST_DONE,
        )
        when (val settings = countryTaxSettings) {
            is CountryTaxSettings.Spain -> {
                steps += OnboardingStep.SPAIN_STATUS
                if (settings.status == SpainEmploymentStatus.AUTONOMO) {
                    steps += OnboardingStep.SPAIN_REGIME
                    steps += OnboardingStep.SPAIN_START_YEAR
                    steps += OnboardingStep.SPAIN_IAE
                    steps += OnboardingStep.SPAIN_IVA_PAYER
                    steps += OnboardingStep.SPAIN_DECLARATION_TYPE
                }
                if (settings.status != SpainEmploymentStatus.EMPLOYEE) {
                    steps += OnboardingStep.SPAIN_VISA_EXPIRY
                    steps += OnboardingStep.SPAIN_TIE_EXPIRY
                    steps += OnboardingStep.SPAIN_REMIND_VISA
                }
                if (settings.status == SpainEmploymentStatus.AUTONOMO) {
                    steps += OnboardingStep.SPAIN_REMIND_QUARTERLY
                    steps += OnboardingStep.SPAIN_REMIND_RENTA
                }
            }
            is CountryTaxSettings.Serbia -> {
                steps += OnboardingStep.SERBIA_STATUS
                if (settings.status == SerbiaEmploymentStatus.SOLE_TRADER) {
                    steps += OnboardingStep.SERBIA_PAUSHALNI
                }
                if (settings.status == SerbiaEmploymentStatus.SOLE_TRADER ||
                    settings.status == SerbiaEmploymentStatus.DOO
                ) {
                    steps += OnboardingStep.SERBIA_VAT
                }
                steps += OnboardingStep.SERBIA_VISA_EXPIRY
                steps += OnboardingStep.SERBIA_REMIND_VISA
            }
            is CountryTaxSettings.Armenia -> {
                steps += OnboardingStep.ARMENIA_STATUS
                steps += OnboardingStep.ARMENIA_IT_ZONE
                steps += OnboardingStep.ARMENIA_VAT
            }
            is CountryTaxSettings.None -> {}
        }
        return steps
    }
}

class OnboardingViewModel(
    private val repository: ISettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun next() {
        val state = _uiState.value
        val steps = state.activeSteps()
        val nextStep = steps.getOrNull(steps.indexOf(state.step) + 1)
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

    fun selectCountryOfResidence(value: CountryOfResidence) =
        _uiState.update { it.copy(countryOfResidence = value) }

    fun selectCitizenship(value: Citizenship) =
        _uiState.update { it.copy(citizenship = value) }

    fun selectCurrency(value: Currency) =
        _uiState.update { it.copy(defaultCurrency = value) }

    fun selectTaxResidency(value: TaxResidency) {
        val settings = when (value) {
            TaxResidency.SPAIN -> CountryTaxSettings.Spain()
            TaxResidency.SERBIA -> CountryTaxSettings.Serbia()
            TaxResidency.ARMENIA -> CountryTaxSettings.Armenia()
            else -> CountryTaxSettings.None
        }
        _uiState.update { it.copy(taxResidency = value, countryTaxSettings = settings) }
    }

    // ── Испания ───────────────────────────────────────────────────────────────

    fun updateSpainStatus(value: SpainEmploymentStatus) {
        updateSpain { copy(status = value) }
        // Если текущий шаг выпал из activeSteps — вернуться к выбору статуса
        val newState = _uiState.value
        if (newState.step !in newState.activeSteps()) {
            _uiState.update { it.copy(step = OnboardingStep.SPAIN_STATUS) }
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

    // ── Сербия ────────────────────────────────────────────────────────────────

    fun updateSerbiaStatus(value: SerbiaEmploymentStatus) {
        updateSerbia { copy(status = value) }
        val newState = _uiState.value
        if (newState.step !in newState.activeSteps()) {
            _uiState.update { it.copy(step = OnboardingStep.SERBIA_STATUS) }
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
                    countryOfResidence = state.countryOfResidence,
                    citizenship = state.citizenship,
                    defaultCurrency = state.defaultCurrency,
                )
            )
            repository.upsertTaxProfile(
                TaxProfile(
                    taxResidency = state.taxResidency,
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
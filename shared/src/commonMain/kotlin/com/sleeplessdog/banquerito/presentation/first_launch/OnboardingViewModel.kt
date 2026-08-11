package com.sleeplessdog.banquerito.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplessdog.banquerito.data.interfaces.ISettingsRepository
import com.sleeplessdog.banquerito.domain.model.Citizenship
import com.sleeplessdog.banquerito.domain.model.CountryOfResidence
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.TaxProfile
import com.sleeplessdog.banquerito.domain.model.TaxResidency
import com.sleeplessdog.banquerito.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep(val index: Int) {
    WELCOME(0),
    COUNTRY_OF_RESIDENCE(1),
    CITIZENSHIP(2),
    DEFAULT_CURRENCY(3),
    TAX_RESIDENCY(4),
}

// Шагов выбора (без Welcome) — нужно для прогресс-бара
val ONBOARDING_SELECTION_STEPS = OnboardingStep.entries.size - 1

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val countryOfResidence: CountryOfResidence = CountryOfResidence.SPAIN,
    val citizenship: Citizenship = Citizenship.OTHER,
    val defaultCurrency: Currency = Currency.EUR,
    val taxResidency: TaxResidency = TaxResidency.SPAIN,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
)

class OnboardingViewModel(
    private val repository: ISettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun next() {
        val current = _uiState.value.step
        val next = OnboardingStep.entries.getOrNull(current.ordinal + 1)
        if (next != null) {
            _uiState.update { it.copy(step = next) }
        } else {
            finishOnboarding()
        }
    }

    fun back(): Boolean {
        val current = _uiState.value.step
        if (current == OnboardingStep.WELCOME) return false
        val prev = OnboardingStep.entries.getOrNull(current.ordinal - 1) ?: return false
        _uiState.update { it.copy(step = prev) }
        return true
    }

    fun selectCountryOfResidence(value: CountryOfResidence) {
        _uiState.update { it.copy(countryOfResidence = value) }
    }

    fun selectCitizenship(value: Citizenship) {
        _uiState.update { it.copy(citizenship = value) }
    }

    fun selectCurrency(value: Currency) {
        _uiState.update { it.copy(defaultCurrency = value) }
    }

    fun selectTaxResidency(value: TaxResidency) {
        _uiState.update { it.copy(taxResidency = value) }
    }

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
                )
            )
            repository.setFirstLaunchComplete()

            _uiState.update { it.copy(isSaving = false, isComplete = true) }
        }
    }
}
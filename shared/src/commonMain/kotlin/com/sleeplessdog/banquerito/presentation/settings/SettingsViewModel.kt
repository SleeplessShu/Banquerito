package com.sleeplessdog.banquerito.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplessdog.banquerito.data.interfaces.ISettingsRepository
import com.sleeplessdog.banquerito.data.repository.SettingsRepository
import com.sleeplessdog.banquerito.domain.model.CountryTaxSettings
import com.sleeplessdog.banquerito.domain.model.TaxProfile
import com.sleeplessdog.banquerito.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userProfile: UserProfile = UserProfile(),
    val taxProfile: TaxProfile = TaxProfile(),
    val isLoading: Boolean = false,
)

class SettingsViewModel(
    private val repository: ISettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            launch {
                repository.getUserProfile().collect { profile ->
                    _uiState.update { it.copy(userProfile = profile) }
                }
            }
            launch {
                repository.getTaxProfile().collect { profile ->
                    _uiState.update { it.copy(taxProfile = profile) }
                }
            }
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.upsertUserProfile(profile)
        }
    }

    fun saveTaxProfile(profile: TaxProfile) {
        viewModelScope.launch {
            repository.upsertTaxProfile(profile)
        }
    }

    fun updateCountryTaxSettings(settings: CountryTaxSettings) {
        viewModelScope.launch {
            val updated = _uiState.value.taxProfile.copy(countryTaxSettings = settings)
            repository.upsertTaxProfile(updated)
        }
    }
}
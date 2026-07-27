package com.sleeplessdog.banquerito.presentation.taxes


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeplessdog.banquerito.data.TaxCalculator
import com.sleeplessdog.banquerito.data.repository.AccountRepository
import com.sleeplessdog.banquerito.data.repository.SettingsRepository
import com.sleeplessdog.banquerito.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class TaxesUiState(
    val actualIncome: Double = 0.0,
    val sliderIncome: Double? = null, // null = используем actualIncome
    val currency: Currency = Currency.EUR,
    val calculation: TaxCalculation? = null,
    val deadlines: List<TaxDeadline> = emptyList(),
    val taxAccountIds: List<String> = emptyList(),
    val isLoading: Boolean = false,
) {
    val effectiveIncome: Double get() = sliderIncome ?: actualIncome
}

class TaxesViewModel(
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaxesUiState())
    val uiState: StateFlow<TaxesUiState> = _uiState.asStateFlow()

    private var currentTaxProfile: TaxProfile = TaxProfile()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                settingsRepository.getTaxProfile(),
                settingsRepository.getUserProfile(),
                settingsRepository.getTaxAccountIds(),
            ) { taxProfile, userProfile, taxAccountIds ->
                Triple(taxProfile, userProfile, taxAccountIds)
            }.collect { (taxProfile, userProfile, taxAccountIds) ->
                currentTaxProfile = taxProfile

                val accounts = accountRepository.getAllAccounts().first()
                val relevantAccounts = accounts.filter { it.id in taxAccountIds }

                val income = calculateIncomeForCurrentQuarter(relevantAccounts.map { it.id })

                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val deadlines = TaxCalculator.getUpcomingDeadlines(taxProfile, today)

                _uiState.update {
                    val effectiveIncome = it.sliderIncome ?: income
                    it.copy(
                        actualIncome = income,
                        currency = userProfile.defaultCurrency,
                        calculation = TaxCalculator.calculate(
                            effectiveIncome, userProfile.defaultCurrency, taxProfile
                        ),
                        deadlines = deadlines,
                        taxAccountIds = taxAccountIds,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private suspend fun calculateIncomeForCurrentQuarter(accountIds: List<String>): Double {
        if (accountIds.isEmpty()) return 0.0

        var total = 0.0
        for (accountId in accountIds) {
            val transactions = accountRepository.getTransactionsByAccount(accountId).first()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val quarterStart = quarterStartDate(today)

            transactions
                .filter { it.type == TransactionType.INCOME && it.date >= quarterStart }
                .forEach { total += it.amount }
        }
        return total
    }

    private fun quarterStartDate(today: LocalDate): LocalDate {
        val quarterStartMonth = ((today.monthNumber - 1) / 3) * 3 + 1
        return LocalDate(today.year, quarterStartMonth, 1)
    }

    fun updateSliderIncome(amount: Double) {
        _uiState.update { state ->
            state.copy(
                sliderIncome = amount,
                calculation = TaxCalculator.calculate(amount, state.currency, currentTaxProfile)
            )
        }
    }

    fun resetSliderToActual() {
        _uiState.update { state ->
            state.copy(
                sliderIncome = null,
                calculation = TaxCalculator.calculate(state.actualIncome, state.currency, currentTaxProfile)
            )
        }
    }
}
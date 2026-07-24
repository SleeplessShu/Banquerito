package com.sleeplessdog.banquerito.di

import com.sleeplessdog.banquerito.data.DatabaseDriverFactory
import com.sleeplessdog.banquerito.data.remote.ClaudeApi
import com.sleeplessdog.banquerito.data.remote.ExchangeRateApi
import com.sleeplessdog.banquerito.data.repository.AccountRepository
import com.sleeplessdog.banquerito.data.repository.ExchangeRateRepository
import com.sleeplessdog.banquerito.data.repository.PlannedPaymentRepository
import com.sleeplessdog.banquerito.data.repository.SettingsRepository
import com.sleeplessdog.banquerito.db.BanqueritoDB
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
import com.sleeplessdog.banquerito.presentation.consultant.ConsultantViewModel
import com.sleeplessdog.banquerito.presentation.planning.PlannedPaymentViewModel
import com.sleeplessdog.banquerito.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single<BanqueritoDB> { BanqueritoDB(get<DatabaseDriverFactory>().createDriver()) }
    single { AccountRepository(get()) }
    single { PlannedPaymentRepository(get()) }
    single { SettingsRepository(get()) }
    single { ExchangeRateApi() }
    single { ExchangeRateRepository(get()) }
    single { ClaudeApi() }

    viewModel {
        ConsultantViewModel(
            accountRepository = get(),
            settingsRepository = get(),
            exchangeRateRepository = get(),
            claudeApi = get(),
            apiKey = get(named("anthropicKey")),
        )
    }
    viewModelOf(::AccountsViewModel)
    viewModelOf(::PlannedPaymentViewModel)
    viewModelOf(::SettingsViewModel)
}
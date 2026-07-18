package com.sleeplessdog.banquerito.di

import com.sleeplessdog.banquerito.data.DatabaseDriverFactory
import com.sleeplessdog.banquerito.data.remote.ExchangeRateApi
import com.sleeplessdog.banquerito.data.repository.AccountRepository
import com.sleeplessdog.banquerito.data.repository.ExchangeRateRepository
import com.sleeplessdog.banquerito.data.repository.PlannedPaymentRepository
import com.sleeplessdog.banquerito.data.repository.SettingsRepository
import com.sleeplessdog.banquerito.db.BanqueritoDB
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
import com.sleeplessdog.banquerito.presentation.planning.PlannedPaymentViewModel
import com.sleeplessdog.banquerito.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<BanqueritoDB> { BanqueritoDB(get<DatabaseDriverFactory>().createDriver()) }
    single { AccountRepository(get()) }
    single { PlannedPaymentRepository(get()) }
    single { SettingsRepository(get()) }
    single { ExchangeRateApi() }
    single { ExchangeRateRepository(get()) }
    viewModelOf(::AccountsViewModel)
    viewModelOf(::PlannedPaymentViewModel)
    viewModelOf(::SettingsViewModel)
}
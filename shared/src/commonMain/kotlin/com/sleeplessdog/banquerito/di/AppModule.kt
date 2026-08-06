package com.sleeplessdog.banquerito.di

import com.sleeplessdog.banquerito.data.DatabaseDriverFactory
import com.sleeplessdog.banquerito.data.interfaces.IAccountRepository
import com.sleeplessdog.banquerito.data.interfaces.IChatRepository
import com.sleeplessdog.banquerito.data.interfaces.IExchangeRateRepository
import com.sleeplessdog.banquerito.data.interfaces.IPlannedPaymentRepository
import com.sleeplessdog.banquerito.data.interfaces.ISettingsRepository
import com.sleeplessdog.banquerito.data.remote.ClaudeApi
import com.sleeplessdog.banquerito.data.remote.ExchangeRateApi
import com.sleeplessdog.banquerito.data.repository.AccountRepository
import com.sleeplessdog.banquerito.data.repository.ChatRepository
import com.sleeplessdog.banquerito.data.repository.ExchangeRateRepository
import com.sleeplessdog.banquerito.data.repository.PlannedPaymentRepository
import com.sleeplessdog.banquerito.data.repository.SettingsRepository
import com.sleeplessdog.banquerito.db.BanqueritoDB
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
import com.sleeplessdog.banquerito.presentation.consultant.ConsultantViewModel
import com.sleeplessdog.banquerito.presentation.planning.PlannedPaymentViewModel
import com.sleeplessdog.banquerito.presentation.settings.SettingsViewModel
import com.sleeplessdog.banquerito.presentation.taxes.TaxesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single<BanqueritoDB> { BanqueritoDB(get<DatabaseDriverFactory>().createDriver()) }
    single<IAccountRepository> { AccountRepository(get()) }
    single<ISettingsRepository> { SettingsRepository(get()) }
    single<IPlannedPaymentRepository> { PlannedPaymentRepository(get()) }
    single<IExchangeRateRepository> { ExchangeRateRepository(get()) }
    single<IChatRepository> { ChatRepository(get()) }

    single { ExchangeRateApi() }
    single { ClaudeApi() }

    viewModel {
        ConsultantViewModel(
            accountRepository = get(),
            settingsRepository = get(),
            exchangeRateRepository = get(),
            chatRepository = get(),
            fileStorage = get(),
            claudeApi = get(),
            apiKey = get(named("anthropicKey")),
        )
    }
    viewModelOf(::AccountsViewModel)
    viewModelOf(::PlannedPaymentViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::TaxesViewModel)
}
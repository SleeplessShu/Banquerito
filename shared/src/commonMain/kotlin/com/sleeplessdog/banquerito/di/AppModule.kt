package com.sleeplessdog.banquerito.di

import com.sleeplessdog.banquerito.data.DatabaseDriverFactory
import com.sleeplessdog.banquerito.data.repository.AccountRepository
import com.sleeplessdog.banquerito.data.repository.PlannedPaymentRepository
import com.sleeplessdog.banquerito.db.BanqueritoDB
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
import com.sleeplessdog.banquerito.presentation.planing.PlannedPaymentViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<BanqueritoDB> { BanqueritoDB(get<DatabaseDriverFactory>().createDriver()) }
    single { AccountRepository(get()) }
    single { PlannedPaymentRepository(get()) }
    viewModelOf(::AccountsViewModel)
    viewModelOf(::PlannedPaymentViewModel)
}
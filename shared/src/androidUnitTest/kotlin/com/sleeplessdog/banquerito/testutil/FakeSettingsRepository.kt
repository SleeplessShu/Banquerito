package com.sleeplessdog.banquerito.testutil

import com.sleeplessdog.banquerito.data.interfaces.ISettingsRepository

import com.sleeplessdog.banquerito.domain.model.*

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class FakeSettingsRepository : ISettingsRepository {

    val userProfileFlow = MutableStateFlow(UserProfile())
    val taxProfileFlow = MutableStateFlow(TaxProfile())
    val taxAccountIdsFlow = MutableStateFlow<List<String>>(emptyList())

    override fun getUserProfile(): Flow<UserProfile> = userProfileFlow.asStateFlow()
    override fun getTaxProfile(): Flow<TaxProfile> = taxProfileFlow.asStateFlow()
    override fun getTaxAccountIds(): Flow<List<String>> = taxAccountIdsFlow.asStateFlow()

    override suspend fun upsertUserProfile(profile: UserProfile) {
        userProfileFlow.value = profile
    }

    override suspend fun upsertTaxProfile(profile: TaxProfile) {
        taxProfileFlow.value = profile
    }

    override suspend fun addTaxAccount(accountId: String) {
        taxAccountIdsFlow.value = taxAccountIdsFlow.value + accountId
    }

    override suspend fun removeTaxAccount(accountId: String) {
        taxAccountIdsFlow.value = taxAccountIdsFlow.value - accountId
    }

    override suspend fun toggleTaxAccount(accountId: String, include: Boolean) {
        if (include) addTaxAccount(accountId) else removeTaxAccount(accountId)
    }
}
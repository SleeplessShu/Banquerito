package com.sleeplessdog.banquerito.data.interfaces


import com.sleeplessdog.banquerito.domain.model.TaxProfile
import com.sleeplessdog.banquerito.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {

    fun getUserProfile(): Flow<UserProfile>

    fun getTaxProfile(): Flow<TaxProfile>

    fun getTaxAccountIds(): Flow<List<String>>

    suspend fun upsertUserProfile(profile: UserProfile)

    suspend fun upsertTaxProfile(profile: TaxProfile)

    suspend fun addTaxAccount(accountId: String)

    suspend fun removeTaxAccount(accountId: String)

    suspend fun toggleTaxAccount(accountId: String, include: Boolean)
}
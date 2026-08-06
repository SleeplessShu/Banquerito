package com.sleeplessdog.banquerito.presentation


import com.sleeplessdog.banquerito.domain.model.*
import com.sleeplessdog.banquerito.presentation.settings.SettingsViewModel
import com.sleeplessdog.banquerito.testutil.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: FakeSettingsRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = FakeSettingsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(settingsRepository)

    @Test
    fun `loads user profile on init`() = runTest {
        settingsRepository.userProfileFlow.value = UserProfile(name = "Dmitry")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Dmitry", viewModel.uiState.value.userProfile.name)
    }

    @Test
    fun `loads tax profile on init`() = runTest {
        settingsRepository.taxProfileFlow.value = TaxProfile(taxResidency = TaxResidency.SPAIN)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TaxResidency.SPAIN, viewModel.uiState.value.taxProfile.taxResidency)
    }

    @Test
    fun `saveUserProfile persists to repository`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val newProfile = UserProfile(name = "New Name", defaultCurrency = Currency.USD)
        viewModel.saveUserProfile(newProfile)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("New Name", settingsRepository.userProfileFlow.value.name)
        assertEquals(Currency.USD, settingsRepository.userProfileFlow.value.defaultCurrency)
    }

    @Test
    fun `saveTaxProfile persists to repository`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val newProfile = TaxProfile(taxResidency = TaxResidency.SERBIA)
        viewModel.saveTaxProfile(newProfile)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TaxResidency.SERBIA, settingsRepository.taxProfileFlow.value.taxResidency)
    }

    @Test
    fun `updateCountryTaxSettings preserves other tax profile fields`() = runTest {
        settingsRepository.taxProfileFlow.value = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            remindQuarterlyDays = 10,
            remindRentaDays = 20,
        )
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val newSettings = CountryTaxSettings.Spain(
            status = SpainEmploymentStatus.AUTONOMO,
            autonomoRegime = SpainAutonomoRegime.TARIFA_PLANA,
        )
        viewModel.updateCountryTaxSettings(newSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = settingsRepository.taxProfileFlow.value
        assertIs<CountryTaxSettings.Spain>(result.countryTaxSettings)
        assertEquals(TaxResidency.SPAIN, result.taxResidency)
        assertEquals(10, result.remindQuarterlyDays)
        assertEquals(20, result.remindRentaDays)
    }

    @Test
    fun `updateCountryTaxSettings switches from Serbia to Armenia settings`() = runTest {
        settingsRepository.taxProfileFlow.value = TaxProfile(
            taxResidency = TaxResidency.SERBIA,
            countryTaxSettings = CountryTaxSettings.Serbia()
        )
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateCountryTaxSettings(CountryTaxSettings.Armenia(itZone = true))
        testDispatcher.scheduler.advanceUntilIdle()

        val result = settingsRepository.taxProfileFlow.value.countryTaxSettings
        assertIs<CountryTaxSettings.Armenia>(result)
    }
}
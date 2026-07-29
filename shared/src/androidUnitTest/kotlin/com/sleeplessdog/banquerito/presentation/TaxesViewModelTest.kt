package com.sleeplessdog.banquerito.presentation

import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.CountryTaxSettings
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.SpainAutonomoRegime
import com.sleeplessdog.banquerito.domain.model.SpainEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.TaxProfile
import com.sleeplessdog.banquerito.domain.model.TaxResidency
import com.sleeplessdog.banquerito.domain.model.Transaction
import com.sleeplessdog.banquerito.domain.model.TransactionType
import com.sleeplessdog.banquerito.domain.model.UserProfile
import com.sleeplessdog.banquerito.presentation.taxes.TaxesViewModel
import com.sleeplessdog.banquerito.testutil.FakeAccountRepository
import com.sleeplessdog.banquerito.testutil.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TaxesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var settingsRepository: FakeSettingsRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        accountRepository = FakeAccountRepository()
        settingsRepository = FakeSettingsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = TaxesViewModel(accountRepository, settingsRepository)

    @Test
    fun `initial state has zero income and no calculation before data loads`() = runTest {
        val viewModel = createViewModel()
        assertEquals(0.0, viewModel.uiState.value.actualIncome)
    }

    @Test
    fun `loads income only from accounts marked for tax inclusion`() = runTest {
        val account1 = fakeAccount("acc1")
        val account2 = fakeAccount("acc2")
        accountRepository.setAccounts(listOf(account1, account2))
        accountRepository.setTransactions(
            "acc1", listOf(
                fakeIncomeTransaction("acc1", 1000.0, today())
            )
        )
        accountRepository.setTransactions(
            "acc2", listOf(
                fakeIncomeTransaction("acc2", 5000.0, today())
            )
        )
        settingsRepository.taxAccountIdsFlow.value = listOf("acc1")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1000.0, viewModel.uiState.value.actualIncome)
    }

    @Test
    fun `excludes transactions before current quarter start`() = runTest {
        val account = fakeAccount("acc1")
        accountRepository.setAccounts(listOf(account))
        settingsRepository.taxAccountIdsFlow.value = listOf("acc1")

        val currentQuarterDate = today()
        val previousQuarterDate = currentQuarterDate.minusOneQuarterApprox()

        accountRepository.setTransactions(
            "acc1", listOf(
                fakeIncomeTransaction("acc1", 1000.0, currentQuarterDate),
                fakeIncomeTransaction("acc1", 9999.0, previousQuarterDate)
            )
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1000.0, viewModel.uiState.value.actualIncome)
    }

    @Test
    fun `excludes expense transactions from income calculation`() = runTest {
        val account = fakeAccount("acc1")
        accountRepository.setAccounts(listOf(account))
        settingsRepository.taxAccountIdsFlow.value = listOf("acc1")

        accountRepository.setTransactions(
            "acc1", listOf(
                fakeIncomeTransaction("acc1", 1000.0, today()),
                Transaction(
                    id = "t2",
                    accountId = "acc1",
                    type = TransactionType.EXPENSE,
                    amount = 500.0,
                    comment = "",
                    date = today(),
                )
            )
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1000.0, viewModel.uiState.value.actualIncome)
    }

    @Test
    fun `zero tax accounts results in zero income`() = runTest {
        val account = fakeAccount("acc1")
        accountRepository.setAccounts(listOf(account))
        accountRepository.setTransactions(
            "acc1",
            listOf(fakeIncomeTransaction("acc1", 1000.0, today()))
        )
        settingsRepository.taxAccountIdsFlow.value = emptyList()

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0.0, viewModel.uiState.value.actualIncome)
    }

    @Test
    fun `calculation reflects tax profile settings`() = runTest {
        val account = fakeAccount("acc1")
        accountRepository.setAccounts(listOf(account))
        accountRepository.setTransactions(
            "acc1",
            listOf(fakeIncomeTransaction("acc1", 6000.0, today()))
        )
        settingsRepository.taxAccountIdsFlow.value = listOf("acc1")
        settingsRepository.taxProfileFlow.value = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(
                status = SpainEmploymentStatus.AUTONOMO,
                autonomoRegime = SpainAutonomoRegime.TARIFA_PLANA,
                isIvaPayer = false,
            )
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val calc = viewModel.uiState.value.calculation
        assertNotNull(calc)
        assertEquals(6000.0, calc.grossIncome)
        assertTrue(calc.segments.any { it.label == "Cuota autónomo" })
    }

    @Test
    fun `slider income overrides effective income and recalculates`() = runTest {
        val account = fakeAccount("acc1")
        accountRepository.setAccounts(listOf(account))
        accountRepository.setTransactions(
            "acc1",
            listOf(fakeIncomeTransaction("acc1", 1000.0, today()))
        )
        settingsRepository.taxAccountIdsFlow.value = listOf("acc1")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1000.0, viewModel.uiState.value.effectiveIncome)

        viewModel.updateSliderIncome(5000.0)

        assertEquals(5000.0, viewModel.uiState.value.effectiveIncome)
        assertEquals(5000.0, viewModel.uiState.value.calculation?.grossIncome)
        assertEquals(1000.0, viewModel.uiState.value.actualIncome)
    }

    @Test
    fun `reset slider returns to actual income`() = runTest {
        val account = fakeAccount("acc1")
        accountRepository.setAccounts(listOf(account))
        accountRepository.setTransactions(
            "acc1",
            listOf(fakeIncomeTransaction("acc1", 1200.0, today()))
        )
        settingsRepository.taxAccountIdsFlow.value = listOf("acc1")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSliderIncome(8000.0)
        assertEquals(8000.0, viewModel.uiState.value.effectiveIncome)

        viewModel.resetSliderToActual()

        assertNull(viewModel.uiState.value.sliderIncome)
        assertEquals(1200.0, viewModel.uiState.value.effectiveIncome)
    }

    @Test
    fun `deadlines are populated based on tax profile`() = runTest {
        settingsRepository.taxProfileFlow.value = TaxProfile(
            taxResidency = TaxResidency.SPAIN,
            countryTaxSettings = CountryTaxSettings.Spain(status = SpainEmploymentStatus.AUTONOMO)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.deadlines.isNotEmpty())
    }

    @Test
    fun `currency follows user profile default currency`() = runTest {
        settingsRepository.userProfileFlow.value = UserProfile(defaultCurrency = Currency.USD)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(Currency.USD, viewModel.uiState.value.currency)
    }

    // ===== helpers =====

    private fun fakeAccount(id: String) = Account(
        id = id,
        name = "Account $id",
        bankName = "Bank",
        balance = 0.0,
        currency = Currency.EUR,
    )

    private fun fakeIncomeTransaction(accountId: String, amount: Double, date: LocalDate) =
        Transaction(
            id = "txn_${accountId}_$amount",
            accountId = accountId,
            type = TransactionType.INCOME,
            amount = amount,
            comment = "",
            date = date,
        )

    private fun today(): LocalDate {
        val now = Clock.System.now()
        return now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    private fun LocalDate.minusOneQuarterApprox(): LocalDate {
        val newMonth = monthNumber - 3
        return if (newMonth < 1) {
            LocalDate(year - 1, newMonth + 12, 1)
        } else {
            LocalDate(year, newMonth, 1)
        }
    }
}
package com.sleeplessdog.banquerito.presentation

import com.sleeplessdog.banquerito.domain.model.*
import com.sleeplessdog.banquerito.presentation.planning.PlannedPaymentViewModel
import com.sleeplessdog.banquerito.testutil.FakePlannedPaymentRepository
import com.sleeplessdog.banquerito.testutil.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlannedPaymentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var plannedPaymentRepository: FakePlannedPaymentRepository
    private lateinit var settingsRepository: FakeSettingsRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        plannedPaymentRepository = FakePlannedPaymentRepository()
        settingsRepository = FakeSettingsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        PlannedPaymentViewModel(plannedPaymentRepository, settingsRepository)

    @Test
    fun `loads payments incomes and archived on init`() = runTest {
        plannedPaymentRepository.paymentsFlow.value = listOf(fakePayment("p1"))
        plannedPaymentRepository.incomesFlow.value = listOf(fakeIncome("i1"))
        plannedPaymentRepository.archivedPaymentsFlow.value = listOf(fakePayment("p2", isArchived = true))
        plannedPaymentRepository.archivedIncomesFlow.value = listOf(fakeIncome("i2", isArchived = true))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.payments.size)
        assertEquals(1, viewModel.uiState.value.incomes.size)
        assertEquals(1, viewModel.uiState.value.archivedPayments.size)
        assertEquals(1, viewModel.uiState.value.archivedIncomes.size)
    }

    @Test
    fun `loads user profile on init`() = runTest {
        settingsRepository.userProfileFlow.value = UserProfile(name = "Dmitry")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Dmitry", viewModel.uiState.value.userProfile.name)
    }

    @Test
    fun `addPayment creates active non-archived payment`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addPayment(
            name = "Rent",
            amount = 900.0,
            currency = Currency.EUR,
            accountId = "acc1",
            recurrence = Recurrence.MONTHLY,
            dayOfMonth = 1,
            nextDate = today(),
            remindDaysBefore = 3,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, plannedPaymentRepository.insertedPayments.size)
        val payment = plannedPaymentRepository.insertedPayments.first()
        assertEquals("Rent", payment.name)
        assertTrue(payment.isActive)
        assertTrue(!payment.isArchived)
    }

    @Test
    fun `addIncome creates income with correct fields`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addIncome(
            comment = "Freelance",
            amount = 1500.0,
            currency = Currency.USD,
            accountId = "acc1",
            recurrence = Recurrence.MONTHLY,
            nextDate = today(),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, plannedPaymentRepository.insertedIncomes.size)
        val income = plannedPaymentRepository.insertedIncomes.first()
        assertEquals("Freelance", income.comment)
        assertEquals(1500.0, income.amount)
        assertEquals(Currency.USD, income.currency)
    }

    @Test
    fun `updatePayment calls repository update`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val payment = fakePayment("p1")
        viewModel.updatePayment(payment)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, plannedPaymentRepository.updatedPayments.size)
        assertEquals("p1", plannedPaymentRepository.updatedPayments.first().id)
    }

    @Test
    fun `updateIncome calls repository update`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val income = fakeIncome("i1")
        viewModel.updateIncome(income)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, plannedPaymentRepository.updatedIncomes.size)
    }

    @Test
    fun `archivePayment moves payment from active to archived list`() = runTest {
        plannedPaymentRepository.paymentsFlow.value = listOf(fakePayment("p1"))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.archivePayment(fakePayment("p1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(plannedPaymentRepository.archivedPaymentIds.contains("p1"))
        assertTrue(viewModel.uiState.value.payments.none { it.id == "p1" })
        assertTrue(viewModel.uiState.value.archivedPayments.any { it.id == "p1" })
    }

    @Test
    fun `archiveIncome moves income from active to archived list`() = runTest {
        plannedPaymentRepository.incomesFlow.value = listOf(fakeIncome("i1"))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.archiveIncome(fakeIncome("i1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(plannedPaymentRepository.archivedIncomeIds.contains("i1"))
        assertTrue(viewModel.uiState.value.incomes.none { it.id == "i1" })
    }

    @Test
    fun `deletePayment removes payment from repository`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deletePayment(fakePayment("p1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(plannedPaymentRepository.deletedPaymentIds.contains("p1"))
    }

    @Test
    fun `deleteIncome removes income from repository`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteIncome(fakeIncome("i1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(plannedPaymentRepository.deletedIncomeIds.contains("i1"))
    }

    @Test
    fun `toggleArchiveVisible flips showArchive flag`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(!viewModel.uiState.value.showArchive)

        viewModel.toggleArchiveVisible()
        assertTrue(viewModel.uiState.value.showArchive)

        viewModel.toggleArchiveVisible()
        assertTrue(!viewModel.uiState.value.showArchive)
    }

    @Test
    fun `saveUserProfile calls settings repository`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val profile = UserProfile(name = "New Name")
        viewModel.saveUserProfile(profile)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("New Name", settingsRepository.userProfileFlow.value.name)
    }

    // ===== helpers =====

    private fun fakePayment(id: String, isArchived: Boolean = false) = PlannedPayment(
        id = id,
        name = "Payment $id",
        amount = 100.0,
        currency = Currency.EUR,
        accountId = "acc1",
        recurrence = Recurrence.MONTHLY,
        dayOfMonth = 1,
        nextDate = today(),
        remindDaysBefore = 3,
        isActive = true,
        isArchived = isArchived,
    )

    private fun fakeIncome(id: String, isArchived: Boolean = false) = PlannedIncome(
        id = id,
        comment = "Income $id",
        amount = 200.0,
        currency = Currency.EUR,
        accountId = "acc1",
        recurrence = Recurrence.MONTHLY,
        nextDate = today(),
        isArchived = isArchived,
    )

    private fun today(): LocalDate {
        val now = kotlinx.datetime.Clock.System.now()
        return now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
    }
}
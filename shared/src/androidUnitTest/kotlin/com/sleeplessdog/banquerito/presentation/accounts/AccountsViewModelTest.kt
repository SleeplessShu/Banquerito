package com.sleeplessdog.banquerito.presentation.accounts


import com.sleeplessdog.banquerito.domain.model.*
import com.sleeplessdog.banquerito.testutil.FakeAccountRepository
import com.sleeplessdog.banquerito.testutil.FakeExchangeRateRepository
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
class AccountsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var accountRepository: FakeAccountRepository
    private lateinit var exchangeRateRepository: FakeExchangeRateRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        accountRepository = FakeAccountRepository()
        exchangeRateRepository = FakeExchangeRateRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AccountsViewModel(accountRepository, exchangeRateRepository)

    @Test
    fun `loads accounts on init`() = runTest {
        accountRepository.setAccounts(listOf(fakeAccount("acc1", 1000.0)))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.accounts.size)
        assertEquals("acc1", viewModel.uiState.value.accounts.first().id)
    }

    @Test
    fun `refreshes exchange rates on init`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(exchangeRateRepository.refreshCalled)
    }

    @Test
    fun `setDisplayCurrency updates selected currency`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setDisplayCurrency(Currency.USD)

        assertEquals(Currency.USD, viewModel.uiState.value.selectedCurrency)
    }

    @Test
    fun `addAccount creates account with zero balance`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addAccount("My account", "My Bank", Currency.EUR)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, accountRepository.insertedAccounts.size)
        assertEquals(0.0, accountRepository.insertedAccounts.first().balance)
        assertEquals("My account", accountRepository.insertedAccounts.first().name)
        assertEquals(SimReminderInterval.NEVER, accountRepository.insertedAccounts.first().simReminderInterval)
    }

    @Test
    fun `addTransaction income increases account balance`() = runTest {
        accountRepository.setAccounts(listOf(fakeAccount("acc1", 1000.0)))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addTransaction("acc1", TransactionType.INCOME, 500.0, "salary", today())
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1500.0, accountRepository.updatedBalances["acc1"])
    }

    @Test
    fun `addTransaction expense decreases account balance`() = runTest {
        accountRepository.setAccounts(listOf(fakeAccount("acc1", 1000.0)))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addTransaction("acc1", TransactionType.EXPENSE, 300.0, "rent", today())
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(700.0, accountRepository.updatedBalances["acc1"])
    }

    @Test
    fun `addTransaction for unknown account does not crash`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addTransaction("nonexistent", TransactionType.INCOME, 100.0, "test", today())
        testDispatcher.scheduler.advanceUntilIdle()

        // транзакция создаётся, но баланс не обновляется, т.к. счёт не найден
        assertEquals(1, accountRepository.insertedTransactions.size)
        assertTrue(accountRepository.updatedBalances.isEmpty())
    }

    @Test
    fun `addTransfer moves money between two accounts`() = runTest {
        accountRepository.setAccounts(listOf(
            fakeAccount("from", 1000.0),
            fakeAccount("to", 200.0),
        ))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addTransfer("from", "to", 300.0, "transfer", today())
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(700.0, accountRepository.updatedBalances["from"])
        assertEquals(500.0, accountRepository.updatedBalances["to"])
    }

    @Test
    fun `addTransfer creates two linked transactions`() = runTest {
        accountRepository.setAccounts(listOf(
            fakeAccount("from", 1000.0),
            fakeAccount("to", 200.0),
        ))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addTransfer("from", "to", 300.0, "transfer", today())
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, accountRepository.insertedTransactions.size)
        val outTxn = accountRepository.insertedTransactions.first { it.accountId == "from" }
        val inTxn = accountRepository.insertedTransactions.first { it.accountId == "to" }

        assertEquals(TransactionType.TRANSFER_EXPENSE, outTxn.type)
        assertEquals(TransactionType.TRANSFER_INCOME, inTxn.type)
        assertEquals("to", outTxn.toAccountId)
        assertEquals("from", inTxn.toAccountId)
        assertEquals(300.0, outTxn.amount)
        assertEquals(300.0, inTxn.amount)
    }

    @Test
    fun `deleteTransaction income reverts balance decrease`() = runTest {
        accountRepository.setAccounts(listOf(fakeAccount("acc1", 1500.0)))
        accountRepository.setTransactions("acc1", listOf(
            fakeTransaction("t1", "acc1", TransactionType.INCOME, 500.0)
        ))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadAccountDetail("acc1")
        testDispatcher.scheduler.advanceUntilIdle()

        val transaction = viewModel.detailUiState.value.transactions.first()
        viewModel.deleteTransaction(transaction)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1000.0, accountRepository.updatedBalances["acc1"])
        assertTrue(accountRepository.deletedTransactionIds.contains("t1"))
    }

    @Test
    fun `deleteTransaction expense reverts balance increase`() = runTest {
        accountRepository.setAccounts(listOf(fakeAccount("acc1", 700.0)))
        accountRepository.setTransactions("acc1", listOf(
            fakeTransaction("t1", "acc1", TransactionType.EXPENSE, 300.0)
        ))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadAccountDetail("acc1")
        testDispatcher.scheduler.advanceUntilIdle()

        val transaction = viewModel.detailUiState.value.transactions.first()
        viewModel.deleteTransaction(transaction)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1000.0, accountRepository.updatedBalances["acc1"])
    }

    @Test
    fun `deleteTransaction transfer reverts both accounts`() = runTest {
        accountRepository.setAccounts(listOf(
            fakeAccount("from", 700.0),
            fakeAccount("to", 500.0),
        ))
        accountRepository.setTransactions("from", listOf(
            Transaction(
                id = "t1",
                accountId = "from",
                type = TransactionType.TRANSFER_EXPENSE,
                amount = 300.0,
                comment = "",
                date = today(),
                toAccountId = "to",
            )
        ))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadAccountDetail("from")
        testDispatcher.scheduler.advanceUntilIdle()

        val transaction = viewModel.detailUiState.value.transactions.first()
        viewModel.deleteTransaction(transaction)
        testDispatcher.scheduler.advanceUntilIdle()

        // from получает деньги обратно (было списание -300, значит delta = +300)
        assertEquals(1000.0, accountRepository.updatedBalances["from"])
        // to теряет полученные деньги (было +300, значит -300)
        assertEquals(200.0, accountRepository.updatedBalances["to"])
    }

    @Test
    fun `renameAccount calls repository update`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.renameAccount("acc1", "New name")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("New name", accountRepository.updatedNames["acc1"])
    }

    @Test
    fun `updateAccountBank calls repository update`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateAccountBank("acc1", "New Bank")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("New Bank", accountRepository.updatedBanks["acc1"])
    }

    @Test
    fun `updateSimReminder calls repository update`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSimReminder("acc1", SimReminderInterval.MONTHLY)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SimReminderInterval.MONTHLY, accountRepository.updatedSimReminders["acc1"])
    }

    @Test
    fun `loadAccountDetail loads correct account and transactions`() = runTest {
        accountRepository.setAccounts(listOf(fakeAccount("acc1", 1000.0)))
        accountRepository.setTransactions("acc1", listOf(
            fakeTransaction("t1", "acc1", TransactionType.INCOME, 200.0)
        ))
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadAccountDetail("acc1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("acc1", viewModel.detailUiState.value.account?.id)
        assertEquals(1, viewModel.detailUiState.value.transactions.size)
    }

    @Test
    fun `convertCurrency delegates to exchange rate repository`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.convertCurrency(100.0, "EUR", "USD")

        assertEquals(108.0, result, 0.01)
    }

    // ===== helpers =====

    private fun fakeAccount(id: String, balance: Double) = Account(
        id = id,
        name = "Account $id",
        bankName = "Bank",
        balance = balance,
        currency = Currency.EUR,
    )

    private fun fakeTransaction(
        id: String,
        accountId: String,
        type: TransactionType,
        amount: Double,
    ) = Transaction(
        id = id,
        accountId = accountId,
        type = type,
        amount = amount,
        comment = "",
        date = today(),
    )

    private fun today(): LocalDate {
        val now = kotlinx.datetime.Clock.System.now()
        return now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
    }
}
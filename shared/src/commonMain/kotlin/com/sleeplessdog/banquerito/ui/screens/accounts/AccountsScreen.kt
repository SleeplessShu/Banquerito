package com.sleeplessdog.banquerito.ui.screens.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.SimReminderInterval
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountsScreen(
    onAccountClick: (String) -> Unit,
    viewModel: AccountsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAccount by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddAccount = true }) {
                Text("+", fontSize = 24.sp)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                TotalBalanceHeader(
                    accounts = uiState.accounts,
                    selectedCurrency = uiState.selectedCurrency,
                    onCurrencySelect = { viewModel.setDisplayCurrency(it) }
                )
            }
            item {
                Text(
                    text = "Счета",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(uiState.accounts) { account ->
                AccountCard(
                    account = account,
                    displayCurrency = uiState.selectedCurrency,
                    onClick = { onAccountClick(account.id) }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddAccount) {
        AddAccountSheet(
            onConfirm = { name, bank, currency, simReminder ->
                viewModel.addAccount(name, bank, currency, simReminder)
                showAddAccount = false
            },
            onDismiss = { showAddAccount = false }
        )
    }
}

@Composable
fun TotalBalanceHeader(
    accounts: List<Account>,
    selectedCurrency: Currency,
    onCurrencySelect: (Currency) -> Unit
) {
    val total = accounts.sumOf { convertCurrency(it.balance, it.currency, selectedCurrency) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 20.dp)) {
            Text(
                text = "Общий баланс",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatAmount(total, selectedCurrency),
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            CurrencyCarousel(
                selected = selectedCurrency,
                onSelect = onCurrencySelect
            )
        }
    }
}

@Composable
fun AccountCard(
    account: Account,
    displayCurrency: Currency,
    onClick: () -> Unit
) {
    val convertedBalance = convertCurrency(account.balance, account.currency, displayCurrency)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = account.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${account.bankName} · ${account.currency.code}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatAmount(convertedBalance, displayCurrency),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (convertedBalance < 0)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (account.currency != displayCurrency) {
                    Text(
                        text = formatAmount(account.balance, account.currency),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencyCarousel(
    selected: Currency,
    onSelect: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencies = Currency.entries
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = currencies.indexOfFirst { it == selected }.coerceAtLeast(0)
    )

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(currencies) { currency ->
            val isSelected = currency == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(currency) },
                label = {
                    Text(
                        text = "${currency.symbol} ${currency.code}",
                        fontSize = 13.sp
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountSheet(
    onConfirm: (String, String, Currency, SimReminderInterval ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(Currency.EUR) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var simReminder by remember { mutableStateOf(SimReminderInterval.NEVER) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Новый счёт",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Банк") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCurrencyPicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Валюта", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${selectedCurrency.symbol} ${selectedCurrency.code}")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Напоминание об оплате симкарты",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SimReminderInterval.entries.forEach { interval ->
                    FilterChip(
                        selected = simReminder == interval,
                        onClick = { simReminder = interval },
                        label = { Text(interval.label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (name.isNotBlank() && bankName.isNotBlank()) {
                        onConfirm(name.trim(), bankName.trim(), selectedCurrency, simReminder)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Создать счёт")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun convertCurrency(amount: Double, from: Currency, to: Currency): Double {
    if (from == to) return amount
    // Заглушка — потом подключим реальный API курсов
    val rates = mapOf(
        "EUR" to 1.0,
        "USD" to 0.92,
        "GBP" to 1.17,
        "RUB" to 0.0098,
        "GEL" to 0.37,
        "AMD" to 0.0026,
        "TRY" to 0.028,
        "ILS" to 0.25,
    )
    val inEur = amount / (rates[from.code] ?: 1.0)
    return inEur * (rates[to.code] ?: 1.0)
}

fun formatAmount(amount: Double, currency: Currency): String {
    val sign = if (amount < 0) "− " else ""
    val abs = kotlin.math.abs(amount)
    val whole = abs.toLong()
    val decimal = ((abs - whole) * 100).toLong()
    val formatted = "${whole}.${decimal.toString().padStart(2, '0')}"
    return "$sign${currency.symbol} $formatted"
}
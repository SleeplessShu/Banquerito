package com.sleeplessdog.banquerito.ui.screens.accounts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.SimReminderInterval
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountsScreen(
    onAccountClick: (String) -> Unit,
    viewModel: AccountsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAccount by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                shape = RoundedCornerShape(8.dp), onClick = { showAddAccount = true }) {
                Text("+", fontSize = 24.sp)
            }
        }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                TotalBalanceHeader(
                    accounts = uiState.accounts,
                    selectedCurrency = uiState.selectedCurrency,
                    onCurrencySelect = { viewModel.setDisplayCurrency(it) })
            }

            item {
                ExchangeRatesRow(baseCurrency = uiState.selectedCurrency)
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
                    onClick = { onAccountClick(account.id) })
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddAccount) {
        AddAccountSheet(onConfirm = { name, bank, currency, simReminder ->
            viewModel.addAccount(name, bank, currency, simReminder)
            showAddAccount = false
        }, onDismiss = { showAddAccount = false })
    }
}

@Composable
fun TotalBalanceHeader(
    accounts: List<Account>,
    selectedCurrency: Currency,
    onCurrencySelect: (Currency) -> Unit,
) {
    val total = accounts.sumOf { convertCurrency(it.balance, it.currency, selectedCurrency) }
    var showCurrencyWheel by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Общий баланс",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatAmount(total, selectedCurrency),
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { showCurrencyWheel = true }
            )
        }
    }

    if (showCurrencyWheel) {
        var tempCurrency by remember { mutableStateOf(selectedCurrency) }
        AlertDialog(
            onDismissRequest = { showCurrencyWheel = false },
            title = { Text("Выберите валюту") },
            text = {
                CurrencyWheelPicker(
                    selected = tempCurrency,
                    onSelect = { tempCurrency = it }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onCurrencySelect(tempCurrency)
                    showCurrencyWheel = false
                }) { Text("Выбрать") }
            },
            dismissButton = {
                TextButton(onClick = { showCurrencyWheel = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun AccountCard(
    account: Account,
    displayCurrency: Currency,
    onClick: () -> Unit,
) {
    val convertedBalance = convertCurrency(account.balance, account.currency, displayCurrency)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = account.name, fontSize = 14.sp, fontWeight = FontWeight.Medium
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
                    color = if (convertedBalance < 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
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
fun CurrencyWheelPicker(
    selected: Currency,
    onSelect: (Currency) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currencies = Currency.entries.sortedBy { it.code }
    val itemHeight = 48.dp
    val visibleItems = 5
    val multiplier = 1000
    val totalItems = currencies.size * multiplier
    val startIndex =
        totalItems / 2 - (totalItems / 2 % currencies.size) + currencies.indexOfFirst { it == selected }

    val listState =
        rememberLazyListState(initialFirstVisibleItemIndex = startIndex - visibleItems / 2)
    val coroutineScope = rememberCoroutineScope()

    val centerIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex + visibleItems / 2
        }
    }

    LaunchedEffect(centerIndex) {
        onSelect(currencies[centerIndex % currencies.size])
    }

    Box(
        modifier = modifier.fillMaxWidth().height(itemHeight * visibleItems),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(itemHeight),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            shape = RoundedCornerShape(8.dp)
        ) {}

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(totalItems) { index ->
                val currency = currencies[index % currencies.size]
                val isSelected = index == centerIndex
                Box(
                    modifier = Modifier.fillMaxWidth().height(itemHeight).clickable {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index - visibleItems / 2)
                        }
                    }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${currency.symbol} ${currency.code}",
                        fontSize = if (isSelected) 16.sp else 13.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountSheet(

    onConfirm: (String, String, Currency, SimReminderInterval) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(Currency.EUR) }
    var simReminder by remember { mutableStateOf(SimReminderInterval.NEVER) }
    var showCurrencyWheel by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Новый счёт", fontSize = 16.sp, fontWeight = FontWeight.Medium
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
                modifier = Modifier.fillMaxWidth().clickable { showCurrencyWheel = true }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Валюта", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${selectedCurrency.symbol} ${selectedCurrency.code}")
                }
            }

            if (showCurrencyWheel) {
                var tempCurrency by remember { mutableStateOf(selectedCurrency) }
                AlertDialog(
                    onDismissRequest = { showCurrencyWheel = false },
                    title = { Text("Выберите валюту") },
                    text = {
                        CurrencyWheelPicker(
                            selected = tempCurrency, onSelect = { tempCurrency = it })
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedCurrency = tempCurrency
                            showCurrencyWheel = false
                        }) {
                            Text("Выбрать")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCurrencyWheel = false }) {
                            Text("Отмена")
                        }
                    })
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Напоминание об оплате привязанной симкарты каждые",
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
                        label = {
                            Text(
                                text = interval.label,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier.weight(1f)

                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                shape = RoundedCornerShape(8.dp),

                onClick = {
                    if (name.isNotBlank() && bankName.isNotBlank()) {
                        onConfirm(name.trim(), bankName.trim(), selectedCurrency, simReminder)
                    }
                },
                modifier = Modifier.height(60.dp).fillMaxWidth(),
            ) {
                Text(text = "Создать счёт", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun ExchangeRatesRow(baseCurrency: Currency) {
    val rates = remember(baseCurrency) {
        Currency.entries.filter { it != baseCurrency }.map { currency ->
            val rate = convertCurrency(1.0, baseCurrency, currency)
            Triple(baseCurrency, currency, rate)
        }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(rates) { (from, to, rate) ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val text = "${to.code} ${rate}"
                    Text(
                        text = text,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun convertCurrency(amount: Double, from: Currency, to: Currency): Double {
    if (from == to) return amount

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
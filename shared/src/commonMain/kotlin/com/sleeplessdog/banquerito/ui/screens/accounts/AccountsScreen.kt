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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.*
import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
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
                    rates = uiState.exchangeRates,
                    onCurrencySelect = { viewModel.setDisplayCurrency(it) })
            }

            item {
                ExchangeRatesRow(
                    baseCurrency = uiState.selectedCurrency, rates = uiState.exchangeRates
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
                    rates = uiState.exchangeRates,
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
    rates: Map<String, Double>,
    onCurrencySelect: (Currency) -> Unit,
) {
    val total = accounts.sumOf {
        convertCurrency(it.balance, it.currency, selectedCurrency, rates)
    }
    var showCurrencyWheel by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.accounts_total_balance),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatAmount(total, selectedCurrency),
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { showCurrencyWheel = true })
        }
    }

    if (showCurrencyWheel) {
        var tempCurrency by remember { mutableStateOf(selectedCurrency) }
        AlertDialog(
            onDismissRequest = { showCurrencyWheel = false },
            title = { Text(stringResource(Res.string.accounts_select_currency)) },
            text = {
                CurrencyWheelPicker(
                    selected = tempCurrency, onSelect = { tempCurrency = it })
            },
            confirmButton = {
                TextButton(onClick = {
                    onCurrencySelect(tempCurrency)
                    showCurrencyWheel = false
                }) { Text(stringResource(Res.string.action_select)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCurrencyWheel = false
                }) { Text(stringResource(Res.string.action_cancel)) }
            })
    }
}

@Composable
fun AccountCard(
    account: Account,
    displayCurrency: Currency,
    rates: Map<String, Double>,
    onClick: () -> Unit,
) {
    val convertedBalance =
        convertCurrency(account.balance, account.currency, displayCurrency, rates)

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

@Composable
fun ExchangeRatesRow(
    baseCurrency: Currency,
    rates: Map<String, Double>,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(Currency.entries.filter { it != baseCurrency }.toList()) { currency ->
            val rate = rates[currency.code] ?: return@items
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "${currency.code} ${formatAmount(rate, currency)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

fun convertCurrency(
    amount: Double, from: Currency, to: Currency, rates: Map<String, Double>
): Double {
    if (from == to) return amount
    val fromRate = rates[from.code] ?: 1.0
    val toRate = rates[to.code] ?: 1.0
    val inEur = amount / fromRate
    return inEur * toRate
}

fun formatAmount(amount: Double, currency: Currency): String {
    val sign = if (amount < 0) "− " else ""
    val abs = kotlin.math.abs(amount)
    val whole = abs.toLong()
    val decimal = ((abs - whole) * 100).toLong()
    val formatted = "${whole}.${decimal.toString().padStart(2, '0')}"
    return "$sign${currency.symbol} $formatted"
}
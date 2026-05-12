package com.sleeplessdog.banquerito.ui.screens.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import com.sleeplessdog.banquerito.domain.model.Transaction
import com.sleeplessdog.banquerito.domain.model.TransactionType
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
import com.sleeplessdog.banquerito.ui.BanqueritoColors
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: String,
    onBack: () -> Unit,
    viewModel: AccountsViewModel = koinViewModel()
) {
    val detailState by viewModel.detailUiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showEdit by remember { mutableStateOf(false) }
    var showAddTransaction by remember { mutableStateOf(false) }

    LaunchedEffect(accountId) {
        viewModel.loadAccountDetail(accountId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = detailState.account?.name ?: "",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            text = detailState.account?.bankName ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    detailState.account?.let { account ->
                        if (account.simReminderInterval != SimReminderInterval.NEVER) {
                            AssistChip(
                                onClick = { showEdit = true },
                                label = {
                                    Text(
                                        text = "SIM · ${account.simReminderInterval.label}",
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                    IconButton(onClick = { showEdit = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTransaction = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить операцию")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                detailState.account?.let { account ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = formatAmount(account.balance, account.currency),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium
                            )
                            val displayCurrency = uiState.selectedCurrency
                            if (account.currency != displayCurrency) {
                                val converted = convertCurrency(
                                    account.balance,
                                    account.currency,
                                    displayCurrency
                                )
                                Text(
                                    text = formatAmount(converted, displayCurrency),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${account.bankName} · ${account.currency.code}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "История операций",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (detailState.transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Операций пока нет",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(detailState.transactions) { transaction ->
                    TransactionItem(transaction = transaction)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showEdit) {
        detailState.account?.let { account ->
            AddAccountSheet(
                initial = account,
                onConfirm = { name, bank, currency, simReminder ->
                    viewModel.renameAccount(account.id, name)
                    viewModel.updateAccountBank(account.id, bank)
                    viewModel.updateSimReminder(account.id, simReminder)
                    showEdit = false
                },
                onDismiss = { showEdit = false }
            )
        }
    }

    if (showAddTransaction) {
        AddTransactionSheet(
            onConfirm = { type, amount, comment, date ->
                viewModel.addTransaction(accountId, type, amount, comment, date)
                showAddTransaction = false
            },
            onDismiss = { showAddTransaction = false }
        )
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = if (transaction.type == TransactionType.INCOME)
                    BanqueritoColors.Success
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (transaction.type == TransactionType.INCOME) "↙" else "↗",
                        fontSize = 16.sp
                    )
                }
            }
            Column {
                Text(
                    text = transaction.comment.ifBlank { "Без комментария" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.date.toString(),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = (if (transaction.type == TransactionType.INCOME) "+ " else "− ") +
                    formatAmount(transaction.amount, findCurrencyByTransaction()),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (transaction.type == TransactionType.INCOME)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
    }
}

fun findCurrencyByTransaction() = com.sleeplessdog.banquerito.domain.model.Currency.EUR



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountSheet(
    initial: Account? = null,
    onConfirm: (String, String, Currency, SimReminderInterval) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var bankName by remember { mutableStateOf(initial?.bankName ?: "") }
    var selectedCurrency by remember { mutableStateOf(initial?.currency ?: Currency.EUR) }
    var simReminder by remember { mutableStateOf(initial?.simReminderInterval ?: SimReminderInterval.NEVER) }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = if (initial != null) "Редактировать счёт" else "Новый счёт",
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
                Text(if (initial != null) "Сохранить" else "Создать счёт")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun todayDate(): LocalDate {
    val now = kotlinx.datetime.Clock.System.now()
    return now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onConfirm: (TransactionType, Double, String, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.INCOME) }
    var amount by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayDate()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Новая операция",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = type == TransactionType.INCOME,
                    onClick = { type = TransactionType.INCOME },
                    label = { Text("Доход") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE },
                    label = { Text("Расход") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Сумма") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Комментарий") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull() ?: return@Button
                    onConfirm(type, parsedAmount, comment, date)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
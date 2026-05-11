package com.sleeplessdog.banquerito.ui.screens.accounts

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
import com.sleeplessdog.banquerito.domain.model.Transaction
import com.sleeplessdog.banquerito.domain.model.TransactionType
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
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
    var showRename by remember { mutableStateOf(false) }
    var showAddTransaction by remember { mutableStateOf(false) }

    LaunchedEffect(accountId) {
        viewModel.loadAccountDetail(accountId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detailState.account?.name ?: "",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showRename = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Переименовать")
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

    if (showRename) {
        RenameAccountSheet(
            currentName = detailState.account?.name ?: "",
            onConfirm = { newName ->
                viewModel.renameAccount(accountId, newName)
                showRename = false
            },
            onDismiss = { showRename = false }
        )
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
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer,
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

// заглушка — валюту возьмём из аккаунта когда подключим
fun findCurrencyByTransaction() = com.sleeplessdog.banquerito.domain.model.Currency.EUR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameAccountSheet(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Переименовать счёт",
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
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
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

            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = type == TransactionType.INCOME,
                    onClick = { type = TransactionType.INCOME },
                    label = { Text("Доход") },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                )
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE },
                    label = { Text("Расход") },
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
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

fun todayDate(): LocalDate {
    val now = kotlinx.datetime.Clock.System.now()
    return now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
}
package com.sleeplessdog.banquerito.ui.screens.accounts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.SimReminderInterval
import com.sleeplessdog.banquerito.domain.model.Transaction
import com.sleeplessdog.banquerito.domain.model.TransactionFilter
import com.sleeplessdog.banquerito.domain.model.TransactionType
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
import com.sleeplessdog.banquerito.ui.BanqueritoColors
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: String,
    onBack: () -> Unit,
    viewModel: AccountsViewModel = koinViewModel(),
) {
    val detailState by viewModel.detailUiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showEdit by remember { mutableStateOf(false) }
    var showAddTransaction by remember { mutableStateOf(false) }
    var editTransaction by remember { mutableStateOf<Transaction?>(null) }
    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }

    val filteredTransactions = remember(detailState.transactions, selectedFilter) {
        val sorted = detailState.transactions.sortedByDescending { it.createdAt }
        when (selectedFilter) {
            TransactionFilter.ALL -> sorted
            TransactionFilter.INCOME -> sorted.filter { it.type == TransactionType.INCOME }
            TransactionFilter.EXPENSE -> sorted.filter { it.type == TransactionType.EXPENSE }
            TransactionFilter.TRANSFER -> sorted.filter {
                it.type == TransactionType.TRANSFER_EXPENSE ||
                        it.type == TransactionType.TRANSFER_INCOME
            }
        }
    }

    LaunchedEffect(accountId) {
        viewModel.loadAccountDetail(accountId)
    }

    Scaffold(topBar = {
        TopAppBar(
            windowInsets = WindowInsets(top = 2.dp),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            title = {
                Text(
                    text = detailState.account?.name ?: "",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
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
                            onClick = { showEdit = true }, label = {
                                Text(
                                    text = "SIM · ${account.simReminderInterval.label}",
                                    fontSize = 11.sp
                                )
                            }, modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                IconButton(onClick = { showEdit = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                }
            },
        )
    }, floatingActionButton = {
        FloatingActionButton(
            shape = RoundedCornerShape(8.dp), onClick = { showAddTransaction = true }) {
            Icon(Icons.Default.Add, contentDescription = "Добавить операцию")
        }
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                detailState.account?.let { account ->
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                start = 20.dp, end = 20.dp, bottom = 16.dp
                            )
                        ) {
                            Text(
                                text = formatAmount(account.balance, account.currency),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            val displayCurrency = uiState.selectedCurrency
                            if (account.currency != displayCurrency) {
                                val converted = convertCurrency(
                                    account.balance,
                                    account.currency,
                                    displayCurrency,
                                    uiState.exchangeRates
                                )
                                Text(
                                    text = formatAmount(converted, displayCurrency),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "${account.bankName} · ${account.currency.code}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            item {
                TransactionFilters(
                    selected = selectedFilter, onSelect = { selectedFilter = it })
            }
            item {
                Text(
                    text = "История операций",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Операций пока нет",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        accountCurrency = detailState.account?.currency ?: Currency.EUR,
                        currentAccountId = accountId,
                        allAccounts = uiState.accounts,
                        onEdit = { editTransaction = it },
                        onDelete = { viewModel.deleteTransaction(it) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showEdit) {
        detailState.account?.let { account ->
            AddAccountSheet(initial = account, onConfirm = { name, bank, currency, simReminder ->
                viewModel.renameAccount(account.id, name)
                viewModel.updateAccountBank(account.id, bank)
                viewModel.updateSimReminder(account.id, simReminder)
                showEdit = false
            }, onDismiss = { showEdit = false })
        }
    }

    if (showAddTransaction || editTransaction != null) {
        AddTransactionSheet(
            currentAccountId = accountId,
            allAccounts = uiState.accounts,
            editTransaction = editTransaction,
            onConfirm = { type, amount, comment, date, toAccountId ->
                if (editTransaction != null) {
                    viewModel.updateTransaction(
                        editTransaction!!.copy(
                            type = type,
                            amount = amount,
                            comment = comment,
                            date = date,
                            toAccountId = toAccountId
                        )
                    )
                    editTransaction = null
                } else if (type == TransactionType.TRANSFER_EXPENSE && toAccountId != null) {
                    viewModel.addTransfer(accountId, toAccountId, amount, comment, date)
                } else {
                    viewModel.addTransaction(accountId, type, amount, comment, date)
                }
                showAddTransaction = false
            },
            onDismiss = {
                showAddTransaction = false
                editTransaction = null
            })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    accountCurrency: Currency,
    currentAccountId: String,
    allAccounts: List<Account>,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    val isIncoming = transaction.type == TransactionType.INCOME ||
            transaction.type == TransactionType.TRANSFER_INCOME

    val iconColor = when (transaction.type) {
        TransactionType.INCOME -> BanqueritoColors.Success
        TransactionType.TRANSFER_INCOME -> BanqueritoColors.Success
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
        TransactionType.TRANSFER_EXPENSE -> MaterialTheme.colorScheme.primary
    }

    Box {
        Row(
            modifier = Modifier.fillMaxWidth()
                .combinedClickable(onClick = {}, onLongClick = { showMenu = true })
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
                    color = iconColor,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val symbol = when (transaction.type) {
                            TransactionType.INCOME -> "↙"
                            TransactionType.EXPENSE -> "↗"
                            TransactionType.TRANSFER_INCOME -> "⇄"
                            TransactionType.TRANSFER_EXPENSE -> "⇄"
                        }
                        Text(
                            text = symbol,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Column {
                    Text(
                        text = transaction.comment.ifBlank { "Без комментария" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    val subText = if (transaction.type == TransactionType.TRANSFER_INCOME || transaction.type == TransactionType.TRANSFER_EXPENSE) {
                        val toAccount = allAccounts.find { it.id == transaction.toAccountId }
                        "→ ${toAccount?.name ?: "другой счёт"} · ${transaction.date}"
                    } else {
                        transaction.date.toString()
                    }
                    Text(
                        text = subText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            val prefix = if (isIncoming) "+ " else "− "

            val amountColor = if (isIncoming) BanqueritoColors.Success else MaterialTheme.colorScheme.error

            Text(
                text = prefix + formatAmount(transaction.amount, accountCurrency),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = amountColor
            )
        }

        DropdownMenu(
            expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text("Редактировать") }, onClick = {
                showMenu = false
                onEdit(transaction)
            })
            DropdownMenuItem(
                text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    onDelete(transaction)
                })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountSheet(
    initial: Account? = null,
    onConfirm: (String, String, Currency, SimReminderInterval) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var bankName by remember { mutableStateOf(initial?.bankName ?: "") }
    var selectedCurrency by remember { mutableStateOf(initial?.currency ?: Currency.EUR) }
    var simReminder by remember {
        mutableStateOf(
            initial?.simReminderInterval ?: SimReminderInterval.NEVER
        )
    }
    var showCurrencyWheel by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = if (initial != null) "Редактировать счёт" else "Новый счёт",
                fontSize = 16.sp,
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
                modifier = Modifier.fillMaxWidth().clickable { showCurrencyWheel = true }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Валюта", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${selectedCurrency.symbol} ${selectedCurrency.code}")
                }
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
                shape = RoundedCornerShape(8.dp), onClick = {
                    if (name.isNotBlank() && bankName.isNotBlank()) {
                        onConfirm(name.trim(), bankName.trim(), selectedCurrency, simReminder)
                    }
                }, modifier = Modifier.height(60.dp).fillMaxWidth()
            ) {
                Text(
                    text = if (initial != null) "Сохранить" else "Создать счёт", fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(60.dp))
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
                }) { Text("Выбрать") }
            },
            dismissButton = {
                TextButton(onClick = { showCurrencyWheel = false }) { Text("Отмена") }
            })
    }
}

fun todayDate(): LocalDate {
    val now = kotlinx.datetime.Clock.System.now()
    return now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    currentAccountId: String,
    allAccounts: List<Account>,
    editTransaction: Transaction? = null,
    onConfirm: (TransactionType, Double, String, LocalDate, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var type by remember { mutableStateOf(editTransaction?.type ?: TransactionType.INCOME) }
    var amount by remember { mutableStateOf(editTransaction?.amount?.toString() ?: "") }
    var comment by remember { mutableStateOf(editTransaction?.comment ?: "") }
    var date by remember { mutableStateOf(editTransaction?.date ?: todayDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedToAccountId by remember { mutableStateOf(editTransaction?.toAccountId) }

    val otherAccounts = allAccounts.filter { it.id != currentAccountId }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds()
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = if (editTransaction != null) "Редактировать операцию" else "Новая операция",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // тип операции
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Triple(TransactionType.INCOME, "Доход", BanqueritoColors.Success),
                    Triple(TransactionType.EXPENSE, "Расход", MaterialTheme.colorScheme.error),
                    Triple(TransactionType.TRANSFER_EXPENSE, "Перевод", MaterialTheme.colorScheme.primary),
                ).forEach { (txnType, label, color) ->
                    FilterChip(
                        selected = type == txnType, onClick = { type = txnType }, label = {
                            Text(
                                label,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }, colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ), modifier = Modifier.weight(1f)
                    )
                }
            }
            if (type == TransactionType.TRANSFER_EXPENSE || type == TransactionType.TRANSFER_INCOME) {
                Spacer(modifier = Modifier.height(12.dp))
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = otherAccounts.find { it.id == selectedToAccountId }
                            ?.let { "${it.name} · ${it.bankName}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("На счёт") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (otherAccounts.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Нет других счетов") },
                                onClick = {}
                            )
                        } else {
                            otherAccounts.forEach { account ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(account.name, fontSize = 14.sp)
                                            Text(
                                                "${account.bankName} · ${account.currency.code}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedToAccountId = account.id
                                        expanded = false
                                    },
                                    trailingIcon = {
                                        if (selectedToAccountId == account.id) {
                                            Text("✓", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
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



        Spacer(modifier = Modifier.height(12.dp))
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Дата", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${date.dayOfMonth}.${
                        date.monthNumber.toString().padStart(2, '0')
                    }.${date.year}"
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            shape = RoundedCornerShape(8.dp), onClick = {
                val parsedAmount = amount.toDoubleOrNull() ?: return@Button
                if (type == TransactionType.TRANSFER_EXPENSE && selectedToAccountId == null) return@Button
                onConfirm(type, parsedAmount, comment, date, selectedToAccountId)
            }, modifier = Modifier.height(60.dp).fillMaxWidth()
        ) {
            Text("Сохранить", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}

if (showDatePicker) {
    DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
        TextButton(onClick = {
            datePickerState.selectedDateMillis?.let { millis ->
                date = LocalDate.fromEpochDays((millis / 86400000).toInt())
            }
            showDatePicker = false
        }) { Text("Выбрать") }
    }, dismissButton = {
        TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
    }) {
        DatePicker(state = datePickerState)
    }
}
}

@Composable
fun TransactionFilters(
    selected: TransactionFilter,
    onSelect: (TransactionFilter) -> Unit,
) {
    val filters = listOf(
        TransactionFilter.ALL to "Все",
        TransactionFilter.INCOME to "Доходы",
        TransactionFilter.EXPENSE to "Расходы",
        TransactionFilter.TRANSFER to "Переводы",
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(filters) { (filter, label) ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        }
    }
}
package com.sleeplessdog.banquerito.ui.screens.planning

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.PlannedIncome
import com.sleeplessdog.banquerito.domain.model.PlannedItem
import com.sleeplessdog.banquerito.domain.model.PlannedPayment
import com.sleeplessdog.banquerito.presentation.accounts.AccountsViewModel
import com.sleeplessdog.banquerito.presentation.planning.PlannedPaymentViewModel
import com.sleeplessdog.banquerito.ui.BanqueritoColors
import com.sleeplessdog.banquerito.ui.screens.accounts.convertCurrency
import com.sleeplessdog.banquerito.ui.screens.accounts.formatAmount
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    onSettingsClick: () -> Unit,
    planningViewModel: PlannedPaymentViewModel = koinViewModel(),
    accountsViewModel: AccountsViewModel = koinViewModel(),
) {
    val uiState by planningViewModel.uiState.collectAsState()
    val accountsState by accountsViewModel.uiState.collectAsState()
    var showAddPayment by remember { mutableStateOf(false) }
    var editPayment by remember { mutableStateOf<PlannedPayment?>(null) }
    var editIncome by remember { mutableStateOf<PlannedIncome?>(null) }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    val allItems = remember(uiState.payments, uiState.incomes) {
        val payments = uiState.payments.map { PlannedItem.Payment(it) }
        val incomes = uiState.incomes.map { PlannedItem.Income(it) }
        (payments + incomes).sortedBy { it.nextDate }
    }

    val upcoming = allItems.filter {
        val daysUntil = (it.nextDate.toEpochDays() - today.toEpochDays()).toInt()
        daysUntil in 0..30
    }

    val rest = allItems.filter {
        val daysUntil = (it.nextDate.toEpochDays() - today.toEpochDays()).toInt()
        daysUntil > 30
    }

    val allArchived = remember(uiState.archivedPayments, uiState.archivedIncomes) {
        val payments = uiState.archivedPayments.map { PlannedItem.Payment(it) }
        val incomes = uiState.archivedIncomes.map { PlannedItem.Income(it) }
        (payments + incomes).sortedByDescending { it.nextDate }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                shape = RoundedCornerShape(8.dp),
                onClick = { showAddPayment = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                PlanningHeader(onSettingsClick = onSettingsClick)
            }

            if (upcoming.isNotEmpty()) {
                item {
                    Text(
                        text = "Ближайшие 30 дней",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(items = upcoming) { item ->
                    val account = accountsState.accounts.find { it.id == item.accountId }
                    val daysUntil = (item.nextDate.toEpochDays() - today.toEpochDays()).toInt()
                    val hasEnough = account != null &&
                            account.balance >= convertCurrency(item.amount, item.currency, account.currency, rates = accountsState.exchangeRates)
                    PlannedPaymentItem(
                        item = item,
                        account = account,
                        daysUntil = daysUntil,
                        hasEnough = hasEnough,
                        onEdit = { edited ->
                            when (edited) {
                                is PlannedItem.Payment -> editPayment = edited.payment
                                is PlannedItem.Income -> editIncome = edited.income
                            }
                        },
                        onArchive = { archived ->
                            when (archived) {
                                is PlannedItem.Payment -> planningViewModel.archivePayment(archived.payment)
                                is PlannedItem.Income -> planningViewModel.archiveIncome(archived.income)
                            }
                        },
                        onDelete = { deleted ->
                            when (deleted) {
                                is PlannedItem.Payment -> planningViewModel.deletePayment(deleted.payment)
                                is PlannedItem.Income -> planningViewModel.deleteIncome(deleted.income)
                            }
                        }
                    )
                }
            }

            if (rest.isNotEmpty()) {
                item {
                    Text(
                        text = "Все обязательства",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(items = rest) { item ->
                    val account = accountsState.accounts.find { it.id == item.accountId }
                    val daysUntil = (item.nextDate.toEpochDays() - today.toEpochDays()).toInt()
                    val hasEnough = account != null &&
                            account.balance >= convertCurrency(item.amount, item.currency, account.currency, accountsState.exchangeRates)
                    PlannedPaymentItem(
                        item = item,
                        account = account,
                        daysUntil = daysUntil,
                        hasEnough = hasEnough,
                        onEdit = { edited ->
                            when (edited) {
                                is PlannedItem.Payment -> editPayment = edited.payment
                                is PlannedItem.Income -> editIncome = edited.income
                            }
                        },
                        onArchive = { archived ->
                            when (archived) {
                                is PlannedItem.Payment -> planningViewModel.archivePayment(archived.payment)
                                is PlannedItem.Income -> planningViewModel.archiveIncome(archived.income)
                            }
                        },
                        onDelete = { deleted ->
                            when (deleted) {
                                is PlannedItem.Payment -> planningViewModel.deletePayment(deleted.payment)
                                is PlannedItem.Income -> planningViewModel.deleteIncome(deleted.income)
                            }
                        }
                    )
                }
            }

            if (allItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет запланированных платежей",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (allArchived.isNotEmpty()) {
                item {
                    TextButton(
                        onClick = { planningViewModel.toggleArchiveVisible() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (uiState.showArchive)
                                "Скрыть архив (${allArchived.size})"
                            else
                                "Показать архив (${allArchived.size})",
                            fontSize = 13.sp
                        )
                    }
                }
                if (uiState.showArchive) {
                    items(items = allArchived) { item ->
                        val account = accountsState.accounts.find { it.id == item.accountId }
                        PlannedPaymentItem(
                            item = item,
                            account = account,
                            daysUntil = null,
                            hasEnough = true,
                            onEdit = {},
                            onArchive = {},
                            onDelete = { deleted ->
                                when (deleted) {
                                    is PlannedItem.Payment -> planningViewModel.deletePayment(deleted.payment)
                                    is PlannedItem.Income -> planningViewModel.deleteIncome(deleted.income)
                                }
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddPayment || editPayment != null || editIncome != null) {
        AddPlannedPaymentSheet(
            accounts = accountsState.accounts,
            initialPayment = editPayment,
            initialIncome = editIncome,
            onConfirmPayment = { name, amount, currency, accountId, recurrence, dayOfMonth, nextDate, remindDays ->
                if (editPayment != null) {
                    planningViewModel.updatePayment(
                        editPayment!!.copy(
                            name = name, amount = amount, currency = currency,
                            accountId = accountId, recurrence = recurrence,
                            dayOfMonth = dayOfMonth, nextDate = nextDate,
                            remindDaysBefore = remindDays,
                        )
                    )
                    editPayment = null
                } else {
                    planningViewModel.addPayment(
                        name, amount, currency, accountId,
                        recurrence, dayOfMonth, nextDate, remindDays
                    )
                }
                showAddPayment = false
            },
            onConfirmIncome = { comment, amount, currency, accountId, recurrence, nextDate ->
                if (editIncome != null) {
                    planningViewModel.updateIncome(
                        editIncome!!.copy(
                            comment = comment, amount = amount, currency = currency,
                            accountId = accountId, recurrence = recurrence, nextDate = nextDate,
                        )
                    )
                    editIncome = null
                } else {
                    planningViewModel.addIncome(
                        comment, amount, currency, accountId, recurrence, nextDate
                    )
                }
                showAddPayment = false
            },
            onDismiss = {
                showAddPayment = false
                editPayment = null
                editIncome = null
            }
        )
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlannedPaymentItem(
    item: PlannedItem,
    account: Account?,
    daysUntil: Int?,
    hasEnough: Boolean,
    onEdit: (PlannedItem) -> Unit,
    onArchive: (PlannedItem) -> Unit,
    onDelete: (PlannedItem) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    val isIncome = item is PlannedItem.Income
    val title = when (item) {
        is PlannedItem.Payment -> item.name
        is PlannedItem.Income -> item.comment
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showMenu = true }
                ),
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isIncome) "↙" else "↗",
                            fontSize = 14.sp,
                            color = if (isIncome) BanqueritoColors.Success
                            else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${account?.name ?: "Счёт не найден"} · ${item.recurrence.label}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (daysUntil != null) {
                        Text(
                            text = when (daysUntil) {
                                0 -> "Сегодня"
                                1 -> "Завтра"
                                else -> "Через $daysUntil дней"
                            },
                            fontSize = 11.sp,
                            color = when {
                                daysUntil == 0 -> MaterialTheme.colorScheme.error
                                daysUntil <= 3 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatAmount(item.amount, item.currency),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isIncome) BanqueritoColors.Success
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (daysUntil != null && !isIncome) {
                        Text(
                            text = if (hasEnough) "✓ хватает" else "✗ не хватает",
                            fontSize = 11.sp,
                            color = if (hasEnough) BanqueritoColors.Success
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (!item.isArchived) {
                DropdownMenuItem(
                    text = { Text("Редактировать") },
                    onClick = {
                        showMenu = false
                        onEdit(item)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Архивировать") },
                    onClick = {
                        showMenu = false
                        onArchive(item)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    onDelete(item)
                }
            )
        }
    }
}
@Composable
fun PlanningHeader(
    onSettingsClick: () -> Unit,
) {

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    "Планирование",
                fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Настройки",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            }}}
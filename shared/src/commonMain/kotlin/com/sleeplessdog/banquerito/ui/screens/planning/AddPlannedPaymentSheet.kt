package com.sleeplessdog.banquerito.ui.screens.planning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import banquerito.shared.generated.resources.*
import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.PlannedIncome
import com.sleeplessdog.banquerito.domain.model.PlannedPayment
import com.sleeplessdog.banquerito.domain.model.Recurrence
import com.sleeplessdog.banquerito.ui.BanqueritoColors
import com.sleeplessdog.banquerito.ui.screens.accounts.CurrencyWheelPicker
import com.sleeplessdog.banquerito.ui.screens.accounts.todayDate
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlannedPaymentSheet(
    accounts: List<Account>,
    initialPayment: PlannedPayment? = null,
    initialIncome: PlannedIncome? = null,
    onConfirmPayment: (String, Double, Currency, String, Recurrence, Int, LocalDate, Int) -> Unit,
    onConfirmIncome: (String, Double, Currency, String, Recurrence, LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isIncome by remember { mutableStateOf(initialIncome != null) }

    var name by remember { mutableStateOf(initialPayment?.name ?: "") }
    var paymentAmount by remember { mutableStateOf(initialPayment?.amount?.toString() ?: "") }
    var paymentCurrency by remember { mutableStateOf(initialPayment?.currency ?: Currency.EUR) }
    var paymentAccountId by remember { mutableStateOf(initialPayment?.accountId ?: accounts.firstOrNull()?.id ?: "") }
    var recurrence by remember { mutableStateOf(initialPayment?.recurrence ?: Recurrence.MONTHLY) }
    var dayOfMonth by remember { mutableStateOf(initialPayment?.dayOfMonth?.toString() ?: "1") }
    var paymentDate by remember { mutableStateOf(initialPayment?.nextDate ?: todayDate()) }
    var remindDaysBefore by remember { mutableStateOf(initialPayment?.remindDaysBefore?.toString() ?: "3") }

    var incomeComment by remember { mutableStateOf(initialIncome?.comment ?: "") }
    var incomeAmount by remember { mutableStateOf(initialIncome?.amount?.toString() ?: "") }
    var incomeCurrency by remember { mutableStateOf(initialIncome?.currency ?: Currency.EUR) }
    var incomeAccountId by remember { mutableStateOf(initialIncome?.accountId ?: accounts.firstOrNull()?.id ?: "") }
    var incomeRecurrence by remember { mutableStateOf(initialIncome?.recurrence ?: Recurrence.MONTHLY) }
    var incomeDate by remember { mutableStateOf(initialIncome?.nextDate ?: todayDate()) }

    var showCurrencyWheel by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    )

    val currentAmount = if (isIncome) incomeAmount else paymentAmount
    val currentCurrency = if (isIncome) incomeCurrency else paymentCurrency
    val currentAccountId = if (isIncome) incomeAccountId else paymentAccountId
    val currentRecurrence = if (isIncome) incomeRecurrence else recurrence
    val currentDate = if (isIncome) incomeDate else paymentDate

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isIncome,
                    onClick = { isIncome = false },
                    label = {
                        Text(
                            stringResource(Res.string.planning_obligation),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.error,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isIncome,
                    onClick = { isIncome = true },
                    label = {
                        Text(
                            stringResource(Res.string.planning_income),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BanqueritoColors.Success,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = if (isIncome) incomeComment else name,
                onValueChange = { if (isIncome) incomeComment = it else name = it },
                label = {
                    Text(
                        if (isIncome) stringResource(Res.string.planning_comment)
                        else stringResource(Res.string.planning_name)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currentAmount,
                onValueChange = {
                    val filtered = it.filter { c -> c.isDigit() || c == '.' }
                    if (isIncome) incomeAmount = filtered else paymentAmount = filtered
                },
                label = { Text(stringResource(Res.string.planning_amount)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCurrencyWheel = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(Res.string.planning_currency), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${currentCurrency.symbol} ${currentCurrency.code}")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = !accountExpanded }
            ) {
                OutlinedTextField(
                    value = accounts.find { it.id == currentAccountId }
                        ?.let { "${it.name} · ${it.bankName}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(Res.string.planning_account)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    accounts.forEach { account ->
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
                                if (isIncome) incomeAccountId = account.id
                                else paymentAccountId = account.id
                                accountExpanded = false
                            },
                            trailingIcon = {
                                if (currentAccountId == account.id) {
                                    Text("✓", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.planning_recurrence),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            ) {
                items(Recurrence.entries.toList()) { r ->
                    FilterChip(
                        selected = currentRecurrence == r,
                        onClick = {
                            if (isIncome) incomeRecurrence = r else recurrence = r
                        },
                        label = { Text(r.label, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.planning_date), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${currentDate.dayOfMonth}.${currentDate.monthNumber.toString().padStart(2, '0')}.${currentDate.year}")
                }
            }

            if (!isIncome) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = remindDaysBefore,
                    onValueChange = { remindDaysBefore = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(Res.string.planning_remind_days)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    if (isIncome) {
                        val parsed = incomeAmount.toDoubleOrNull() ?: return@Button
                        if (incomeComment.isBlank() || incomeAccountId.isBlank()) return@Button
                        onConfirmIncome(
                            incomeComment.trim(), parsed, incomeCurrency,
                            incomeAccountId, incomeRecurrence, incomeDate
                        )
                    } else {
                        val parsed = paymentAmount.toDoubleOrNull() ?: return@Button
                        if (name.isBlank() || paymentAccountId.isBlank()) return@Button
                        onConfirmPayment(
                            name.trim(), parsed, paymentCurrency, paymentAccountId,
                            recurrence, dayOfMonth.toIntOrNull() ?: 1,
                            paymentDate, remindDaysBefore.toIntOrNull() ?: 3
                        )
                    }
                },
                modifier = Modifier.height(60.dp).fillMaxWidth()
            ) {
                Text(
                    text = if (initialPayment != null || initialIncome != null)
                        stringResource(Res.string.action_save)
                    else
                        stringResource(Res.string.planning_add),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showCurrencyWheel) {
        var tempCurrency by remember { mutableStateOf(currentCurrency) }
        AlertDialog(
            onDismissRequest = { showCurrencyWheel = false },
            title = { Text(stringResource(Res.string.planning_select_currency)) },
            text = {
                CurrencyWheelPicker(
                    selected = tempCurrency,
                    onSelect = { tempCurrency = it }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isIncome) incomeCurrency = tempCurrency
                    else paymentCurrency = tempCurrency
                    showCurrencyWheel = false
                }) { Text(stringResource(Res.string.action_select)) }
            },
            dismissButton = {
                TextButton(onClick = { showCurrencyWheel = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.fromEpochDays((millis / 86400000).toInt())
                        if (isIncome) incomeDate = date else paymentDate = date
                    }
                    showDatePicker = false
                }) { Text(stringResource(Res.string.action_select)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
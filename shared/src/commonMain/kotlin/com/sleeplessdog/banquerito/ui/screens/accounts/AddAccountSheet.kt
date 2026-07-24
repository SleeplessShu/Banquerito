package com.sleeplessdog.banquerito.ui.screens.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.accounts_bank
import banquerito.shared.generated.resources.accounts_create
import banquerito.shared.generated.resources.accounts_currency
import banquerito.shared.generated.resources.accounts_edit
import banquerito.shared.generated.resources.accounts_name
import banquerito.shared.generated.resources.accounts_new
import banquerito.shared.generated.resources.accounts_select_currency
import banquerito.shared.generated.resources.accounts_sim_reminder
import banquerito.shared.generated.resources.action_cancel
import banquerito.shared.generated.resources.action_save
import banquerito.shared.generated.resources.action_select
import com.sleeplessdog.banquerito.domain.model.Account
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.SimReminderInterval
import org.jetbrains.compose.resources.stringResource

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
                text = if (initial != null) stringResource(Res.string.accounts_edit) else stringResource(Res.string.accounts_new),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.accounts_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text(stringResource(Res.string.accounts_bank)) },
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
                    Text(stringResource(Res.string.accounts_currency), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${selectedCurrency.symbol} ${selectedCurrency.code}")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.accounts_sim_reminder),
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
                    text = if (initial != null) stringResource(Res.string.action_save) else stringResource(Res.string.accounts_create), fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(60.dp))
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
                    selectedCurrency = tempCurrency
                    showCurrencyWheel = false
                }) { Text(stringResource(Res.string.action_select)) }
            },
            dismissButton = {
                TextButton(onClick = { showCurrencyWheel = false }) { Text(stringResource(Res.string.action_cancel)) }
            })
    }
}
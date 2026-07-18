package com.sleeplessdog.banquerito.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.domain.model.*
import com.sleeplessdog.banquerito.presentation.settings.SettingsViewModel
import com.sleeplessdog.banquerito.ui.screens.accounts.CurrencyWheelPicker
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCurrencyWheel by remember { mutableStateOf(false) }

    Scaffold(

    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                SettingsHeader(onBackClick = onBack)
            }
            // личное
            item { SectionHeader("Личное") }

            item {
                SettingsDropdown(
                    label = "Страна проживания",
                    selected = uiState.userProfile.countryOfResidence.label,
                    options = CountryOfResidence.entries.map { it.label },
                    onSelect = { index ->
                        viewModel.saveUserProfile(
                            uiState.userProfile.copy(
                                countryOfResidence = CountryOfResidence.entries[index]
                            )
                        )
                    }
                )
            }

            item {
                SettingsDropdown(
                    label = "Гражданство",
                    selected = uiState.userProfile.citizenship.label,
                    options = Citizenship.entries.map { it.label },
                    onSelect = { index ->
                        viewModel.saveUserProfile(
                            uiState.userProfile.copy(
                                citizenship = Citizenship.entries[index]
                            )
                        )
                    }
                )
            }

            item {
                SettingsCard(
                    label = "Валюта по умолчанию",
                    value = "${uiState.userProfile.defaultCurrency.symbol} ${uiState.userProfile.defaultCurrency.code}",
                    onClick = { showCurrencyWheel = true }
                )
            }

            // налоговое
            item { SectionHeader("Налоговый профиль") }

            item {
                SettingsDropdown(
                    label = "Налоговое резиденство",
                    selected = uiState.taxProfile.taxResidency.label,
                    options = TaxResidency.entries.map { it.label },
                    onSelect = { index ->
                        val newResidency = TaxResidency.entries[index]
                        val newSettings = when (newResidency) {
                            TaxResidency.SPAIN -> CountryTaxSettings.Spain()
                            TaxResidency.SERBIA -> CountryTaxSettings.Serbia()
                            TaxResidency.ARMENIA -> CountryTaxSettings.Armenia()
                            else -> CountryTaxSettings.None
                        }
                        viewModel.saveTaxProfile(
                            uiState.taxProfile.copy(
                                taxResidency = newResidency,
                                countryTaxSettings = newSettings
                            )
                        )
                    }
                )
            }

            // динамические поля по стране
            when (val settings = uiState.taxProfile.countryTaxSettings) {
                is CountryTaxSettings.Spain -> {
                    item { SectionHeader("Испания") }

                    item {
                        SettingsDropdown(
                            label = "Статус",
                            selected = settings.status.label,
                            options = SpainEmploymentStatus.entries.map { it.label },
                            onSelect = { index ->
                                viewModel.updateCountryTaxSettings(
                                    settings.copy(status = SpainEmploymentStatus.entries[index])
                                )
                            }
                        )
                    }

                    // autonomo поля
                    if (settings.status == SpainEmploymentStatus.AUTONOMO

                    ) {
                        item {
                            SettingsDropdown(
                                label = "Режим",
                                selected = settings.autonomoRegime.label,
                                options = SpainAutonomoRegime.entries.map { it.label },
                                onSelect = { index ->
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(autonomoRegime = SpainAutonomoRegime.entries[index])
                                    )
                                }
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = settings.autonomoStartYear?.toString() ?: "",
                                onValueChange = {
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(autonomoStartYear = it.toIntOrNull())
                                    )
                                },
                                label = { Text("Год начала как autónomo") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                singleLine = true
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = settings.epigrafe,
                                onValueChange = {
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(epigrafe = it)
                                    )
                                },
                                label = { Text("Epígrafe IAE") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                singleLine = true
                            )
                        }
                        item {
                            SettingsToggle(
                                label = "Плательщик IVA",
                                checked = settings.isIvaPayer,
                                onCheckedChange = {
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(isIvaPayer = it)
                                    )
                                }
                            )
                        }
                        item {
                            SettingsDropdown(
                                label = "Тип декларации",
                                selected = settings.declarationType.label,
                                options = SpainDeclarationType.entries.map { it.label },
                                onSelect = { index ->
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(declarationType = SpainDeclarationType.entries[index])
                                    )
                                }
                            )
                        }
                    }

                    // визовые поля для всех испанских статусов кроме employee
                    if (settings.status != SpainEmploymentStatus.EMPLOYEE) {
                        item { SectionHeader("Виза и документы") }
                        item {
                            OutlinedTextField(
                                value = settings.visaExpiryDate ?: "",
                                onValueChange = {
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(visaExpiryDate = it.ifBlank { null })
                                    )
                                },
                                label = { Text("Дата окончания визы (дд.мм.гггг)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                singleLine = true
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = settings.tieExpiryDate ?: "",
                                onValueChange = {
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(tieExpiryDate = it.ifBlank { null })
                                    )
                                },
                                label = { Text("Дата окончания TIE (дд.мм.гггг)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                singleLine = true
                            )
                        }
                        item {
                            SettingsSlider(
                                label = "Напомнить о визе за ${settings.remindVisaDays} дней",
                                value = settings.remindVisaDays.toFloat(),
                                range = 7f..90f,
                                onValueChange = {
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(remindVisaDays = it.toInt())
                                    )
                                }
                            )
                        }
                    }

                    // поля для зависимых
                    if (settings.status == SpainEmploymentStatus.DEPENDENT_AUTONOMO ||
                        settings.status == SpainEmploymentStatus.DEPENDENT_NOMAD
                    ) {
                        item { SectionHeader("Данные партнёра") }

                        item {
                            OutlinedTextField(
                                value = settings.partnerVisaExpiryDate ?: "",
                                onValueChange = {
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(partnerVisaExpiryDate = it.ifBlank { null })
                                    )
                                },
                                label = { Text("Дата окончания визы партнёра") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                is CountryTaxSettings.Serbia -> {
                    item { SectionHeader("Сербия") }
                    item {
                        SettingsDropdown(
                            label = "Статус",
                            selected = settings.status.label,
                            options = SerbiaEmploymentStatus.entries.map { it.label },
                            onSelect = { index ->
                                viewModel.updateCountryTaxSettings(
                                    settings.copy(status = SerbiaEmploymentStatus.entries[index])
                                )
                            }
                        )
                    }
                    if (settings.status == SerbiaEmploymentStatus.SOLE_TRADER) {
                        item {
                            SettingsToggle(
                                label = "Паушальный налог",
                                checked = settings.pausalniPorez,
                                onCheckedChange = {
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(pausalniPorez = it)
                                    )
                                }
                            )
                        }
                    }
                    if (settings.status == SerbiaEmploymentStatus.SOLE_TRADER ||
                        settings.status == SerbiaEmploymentStatus.DOO
                    ) {
                        item {
                            SettingsToggle(
                                label = "Плательщик НДС",
                                checked = settings.vatPayer,
                                onCheckedChange = {
                                    viewModel.updateCountryTaxSettings(
                                        settings.copy(vatPayer = it)
                                    )
                                }
                            )
                        }
                    }
                    item { SectionHeader("Виза") }
                    item {
                        OutlinedTextField(
                            value = settings.visaExpiryDate ?: "",
                            onValueChange = {
                                viewModel.updateCountryTaxSettings(
                                    settings.copy(visaExpiryDate = it.ifBlank { null })
                                )
                            },
                            label = { Text("Дата окончания визы (дд.мм.гггг)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            singleLine = true
                        )
                    }
                    item {
                        SettingsSlider(
                            label = "Напомнить о визе за ${settings.remindVisaDays} дней",
                            value = settings.remindVisaDays.toFloat(),
                            range = 7f..90f,
                            onValueChange = {
                                viewModel.updateCountryTaxSettings(
                                    settings.copy(remindVisaDays = it.toInt())
                                )
                            }
                        )
                    }
                }

                is CountryTaxSettings.Armenia -> {
                    item { SectionHeader("Армения") }
                    item {
                        SettingsDropdown(
                            label = "Статус",
                            selected = settings.status.label,
                            options = ArmeniaEmploymentStatus.entries.map { it.label },
                            onSelect = { index ->
                                viewModel.updateCountryTaxSettings(
                                    settings.copy(status = ArmeniaEmploymentStatus.entries[index])
                                )
                            }
                        )
                    }
                    item {
                        SettingsToggle(
                            label = "IT зона",
                            checked = settings.itZone,
                            onCheckedChange = {
                                viewModel.updateCountryTaxSettings(
                                    settings.copy(itZone = it)
                                )
                            }
                        )
                    }
                    item {
                        SettingsToggle(
                            label = "Плательщик НДС",
                            checked = settings.vatPayer,
                            onCheckedChange = {
                                viewModel.updateCountryTaxSettings(
                                    settings.copy(vatPayer = it)
                                )
                            }
                        )
                    }
                }

                is CountryTaxSettings.None -> {}
            }

            // напоминания
            item { SectionHeader("Напоминания") }
            if (uiState.taxProfile.countryTaxSettings is CountryTaxSettings.Spain &&
                (uiState.taxProfile.countryTaxSettings as CountryTaxSettings.Spain).status == SpainEmploymentStatus.AUTONOMO
            ) {
                item {
                    SettingsSlider(
                        label = "Квартальная декларация — за ${uiState.taxProfile.remindQuarterlyDays} дней",
                        value = uiState.taxProfile.remindQuarterlyDays.toFloat(),
                        range = 1f..30f,
                        onValueChange = {
                            viewModel.saveTaxProfile(
                                uiState.taxProfile.copy(remindQuarterlyDays = it.toInt())
                            )
                        }
                    )
                }

                item {
                    SettingsSlider(
                        label = "Renta — за ${uiState.taxProfile.remindRentaDays} дней",
                        value = uiState.taxProfile.remindRentaDays.toFloat(),
                        range = 1f..60f,
                        onValueChange = {
                            viewModel.saveTaxProfile(
                                uiState.taxProfile.copy(remindRentaDays = it.toInt())
                            )
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showCurrencyWheel) {
        var tempCurrency by remember { mutableStateOf(uiState.userProfile.defaultCurrency) }
        AlertDialog(
            onDismissRequest = { showCurrencyWheel = false },
            title = { Text("Валюта по умолчанию") },
            text = {
                CurrencyWheelPicker(
                    selected = tempCurrency,
                    onSelect = { tempCurrency = it }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveUserProfile(
                        uiState.userProfile.copy(defaultCurrency = tempCurrency)
                    )
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
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    },
                    trailingIcon = {
                        if (option == selected) {
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SettingsToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1
        )
    }
}

@Composable
fun SettingsHeader(
    onBackClick: () -> Unit,
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
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "Настройки",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

        }
    }
}
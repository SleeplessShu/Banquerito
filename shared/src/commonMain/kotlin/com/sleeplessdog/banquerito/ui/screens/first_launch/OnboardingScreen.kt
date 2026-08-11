package com.sleeplessdog.banquerito.ui.screens.first_launch


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleeplessdog.banquerito.domain.model.ArmeniaEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.Citizenship
import com.sleeplessdog.banquerito.domain.model.CountryOfResidence
import com.sleeplessdog.banquerito.domain.model.CountryTaxSettings
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.SerbiaEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.SpainAutonomoRegime
import com.sleeplessdog.banquerito.domain.model.SpainDeclarationType
import com.sleeplessdog.banquerito.domain.model.SpainEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.TaxResidency
import com.sleeplessdog.banquerito.presentation.onboarding.OnboardingStep
import com.sleeplessdog.banquerito.presentation.onboarding.OnboardingViewModel
import com.sleeplessdog.banquerito.ui.util.BackHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    BackHandler(enabled = state.step != OnboardingStep.WELCOME) {
        viewModel.back()
    }

    // Прогресс считаем по activeSteps без WELCOME
    val activeSteps = state.activeSteps()
    val progressSteps = activeSteps.filter { it != OnboardingStep.WELCOME }
    val progressIndex = progressSteps.indexOf(state.step).coerceAtLeast(0)
    val progress = if (progressSteps.isEmpty()) 1f
    else (progressIndex + 1).toFloat() / progressSteps.size

    // Текст кнопки
    val isLastStep = activeSteps.indexOf(state.step) == activeSteps.lastIndex
    val buttonText = when {
        state.step == OnboardingStep.ALMOST_DONE -> "Поехали"
        isLastStep -> "Готово"
        else -> "Далее"
    }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (state.step != OnboardingStep.WELCOME) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    val forward = activeSteps.indexOf(targetState) > activeSteps.indexOf(initialState)
                    if (forward) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "onboarding_step",
                modifier = Modifier.weight(1f),
            ) { step ->
                val spain = state.countryTaxSettings as? CountryTaxSettings.Spain
                val serbia = state.countryTaxSettings as? CountryTaxSettings.Serbia
                val armenia = state.countryTaxSettings as? CountryTaxSettings.Armenia

                when (step) {
                    // ── Общие шаги ────────────────────────────────────────────
                    OnboardingStep.WELCOME -> WelcomeContent()

                    OnboardingStep.COUNTRY_OF_RESIDENCE -> SelectionContent(
                        title = "Страна проживания",
                        subtitle = "Где вы сейчас живёте?",
                        options = CountryOfResidence.entries,
                        selected = state.countryOfResidence,
                        label = { it.label },
                        onSelect = viewModel::selectCountryOfResidence,
                    )

                    OnboardingStep.CITIZENSHIP -> SelectionContent(
                        title = "Гражданство",
                        subtitle = "Гражданином какой страны вы являетесь?",
                        options = Citizenship.entries,
                        selected = state.citizenship,
                        label = { it.label },
                        onSelect = viewModel::selectCitizenship,
                    )

                    OnboardingStep.DEFAULT_CURRENCY -> SelectionContent(
                        title = "Валюта по умолчанию",
                        subtitle = "В какой валюте вы ведёте основные расчёты?",
                        options = Currency.entries,
                        selected = state.defaultCurrency,
                        label = { it.code },
                        onSelect = viewModel::selectCurrency,
                    )

                    OnboardingStep.TAX_RESIDENCY -> SelectionContent(
                        title = "Налоговое резидентство",
                        subtitle = "В какой стране вы являетесь налоговым резидентом?",
                        options = TaxResidency.entries,
                        selected = state.taxResidency,
                        label = { it.label },
                        onSelect = viewModel::selectTaxResidency,
                    )

                    OnboardingStep.ALMOST_DONE -> AlmostDoneContent(state.taxResidency)

                    // ── Испания ───────────────────────────────────────────────
                    OnboardingStep.SPAIN_STATUS -> SelectionContent(
                        title = "Статус в Испании",
                        subtitle = "Ваш тип занятости или резидентства",
                        options = SpainEmploymentStatus.entries,
                        selected = spain?.status ?: SpainEmploymentStatus.AUTONOMO,
                        label = { it.label },
                        onSelect = viewModel::updateSpainStatus,
                    )

                    OnboardingStep.SPAIN_REGIME -> SelectionContent(
                        title = "Режим Autónomo",
                        subtitle = "Начали недавно? Возможна Tarifa Plana",
                        options = SpainAutonomoRegime.entries,
                        selected = spain?.autonomoRegime ?: SpainAutonomoRegime.GENERAL,
                        label = { it.label },
                        onSelect = viewModel::updateSpainRegime,
                    )

                    OnboardingStep.SPAIN_START_YEAR -> TextInputContent(
                        title = "Год регистрации Autónomo",
                        subtitle = "Нужно для расчёта Tarifa Plana",
                        placeholder = "Например: 2023",
                        value = spain?.autonomoStartYear?.toString() ?: "",
                        onValueChange = viewModel::updateSpainStartYear,
                    )

                    OnboardingStep.SPAIN_IAE -> TextInputContent(
                        title = "IAE (Epígrafe)",
                        subtitle = "Код вида деятельности в испанском реестре",
                        placeholder = "Например: 763",
                        value = spain?.epigrafe ?: "",
                        onValueChange = viewModel::updateSpainIae,
                    )

                    OnboardingStep.SPAIN_IVA_PAYER -> ToggleContent(
                        title = "Плательщик IVA",
                        subtitle = "Включите, если вы обязаны начислять и платить НДС",
                        label = "Я плачу IVA",
                        checked = spain?.isIvaPayer ?: true,
                        onCheckedChange = viewModel::updateSpainIvaPayer,
                    )

                    OnboardingStep.SPAIN_DECLARATION_TYPE -> SelectionContent(
                        title = "Тип декларации",
                        subtitle = "Modelo 130 — для общего режима, 131 — для модульного",
                        options = SpainDeclarationType.entries,
                        selected = spain?.declarationType ?: SpainDeclarationType.MODELO_130,
                        label = { it.label },
                        onSelect = viewModel::updateSpainDeclarationType,
                    )

                    OnboardingStep.SPAIN_VISA_EXPIRY -> TextInputContent(
                        title = "Дата окончания визы",
                        subtitle = "Мы напомним заранее о продлении (дд.мм.гггг)",
                        placeholder = "01.01.2026",
                        value = spain?.visaExpiryDate ?: "",
                        onValueChange = viewModel::updateSpainVisaExpiry,
                    )

                    OnboardingStep.SPAIN_TIE_EXPIRY -> TextInputContent(
                        title = "Дата окончания TIE",
                        subtitle = "Карточка иностранного резидента (дд.мм.гггг)",
                        placeholder = "01.01.2026",
                        value = spain?.tieExpiryDate ?: "",
                        onValueChange = viewModel::updateSpainTieExpiry,
                    )

                    OnboardingStep.SPAIN_REMIND_VISA -> SliderContent(
                        title = "Напоминание о визе / TIE",
                        subtitle = "За сколько дней до окончания напомнить?",
                        value = (spain?.remindVisaDays ?: 30).toFloat(),
                        range = 7f..90f,
                        label = { "${it.toInt()} дней" },
                        onValueChange = { viewModel.updateSpainRemindVisaDays(it.toInt()) },
                    )

                    OnboardingStep.SPAIN_REMIND_QUARTERLY -> SliderContent(
                        title = "Напоминание о квартальной декларации",
                        subtitle = "За сколько дней до квартального платежа напомнить?",
                        value = state.remindQuarterlyDays.toFloat(),
                        range = 1f..30f,
                        label = { "${it.toInt()} дней" },
                        onValueChange = { viewModel.updateRemindQuarterlyDays(it.toInt()) },
                    )

                    OnboardingStep.SPAIN_REMIND_RENTA -> SliderContent(
                        title = "Напоминание о Renta",
                        subtitle = "За сколько дней до Renta (апрель–июнь) напомнить?",
                        value = state.remindRentaDays.toFloat(),
                        range = 7f..60f,
                        label = { "${it.toInt()} дней" },
                        onValueChange = { viewModel.updateRemindRentaDays(it.toInt()) },
                    )

                    // ── Сербия ────────────────────────────────────────────────
                    OnboardingStep.SERBIA_STATUS -> SelectionContent(
                        title = "Статус в Сербии",
                        subtitle = "Ваш тип занятости",
                        options = SerbiaEmploymentStatus.entries,
                        selected = serbia?.status ?: SerbiaEmploymentStatus.SOLE_TRADER,
                        label = { it.label },
                        onSelect = viewModel::updateSerbiaStatus,
                    )

                    OnboardingStep.SERBIA_PAUSHALNI -> ToggleContent(
                        title = "Паушальный налог",
                        subtitle = "Фиксированная ставка для preduzetnik — упрощённый режим",
                        label = "Паушалац (paušalac)",
                        checked = serbia?.pausalniPorez ?: true,
                        onCheckedChange = viewModel::updateSerbiaPaushalni,
                    )

                    OnboardingStep.SERBIA_VAT -> ToggleContent(
                        title = "Плательщик PDV (НДС)",
                        subtitle = "Включите, если оборот превысил порог или вы добровольно зарегистрированы",
                        label = "Я плачу PDV",
                        checked = serbia?.vatPayer ?: false,
                        onCheckedChange = viewModel::updateSerbiaVat,
                    )

                    OnboardingStep.SERBIA_VISA_EXPIRY -> TextInputContent(
                        title = "Дата окончания визы",
                        subtitle = "Мы напомним заранее о продлении (дд.мм.гггг)",
                        placeholder = "01.01.2026",
                        value = serbia?.visaExpiryDate ?: "",
                        onValueChange = viewModel::updateSerbiaVisaExpiry,
                    )

                    OnboardingStep.SERBIA_REMIND_VISA -> SliderContent(
                        title = "Напоминание о визе",
                        subtitle = "За сколько дней до окончания напомнить?",
                        value = (serbia?.remindVisaDays ?: 30).toFloat(),
                        range = 7f..90f,
                        label = { "${it.toInt()} дней" },
                        onValueChange = { viewModel.updateSerbiaRemindVisaDays(it.toInt()) },
                    )

                    // ── Армения ───────────────────────────────────────────────
                    OnboardingStep.ARMENIA_STATUS -> SelectionContent(
                        title = "Статус в Армении",
                        subtitle = "Ваш тип занятости",
                        options = ArmeniaEmploymentStatus.entries,
                        selected = armenia?.status ?: ArmeniaEmploymentStatus.INDIVIDUAL_ENTREPRENEUR,
                        label = { it.label },
                        onSelect = viewModel::updateArmeniaStatus,
                    )

                    OnboardingStep.ARMENIA_IT_ZONE -> ToggleContent(
                        title = "IT-зона",
                        subtitle = "Льготный режим налогообложения для IT-компаний и ИП",
                        label = "Я резидент IT-зоны",
                        checked = armenia?.itZone ?: false,
                        onCheckedChange = viewModel::updateArmeniaItZone,
                    )

                    OnboardingStep.ARMENIA_VAT -> ToggleContent(
                        title = "Плательщик НДС",
                        subtitle = "Включите, если оборот превысил порог или вы добровольно зарегистрированы",
                        label = "Я плачу НДС",
                        checked = armenia?.vatPayer ?: false,
                        onCheckedChange = viewModel::updateArmeniaVat,
                    )
                }
            }

            Button(
                onClick = viewModel::next,
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = buttonText)
                }
            }
        }
    }
}

// ── Экраны ────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "👋", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Добро пожаловать в Banquerito",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Давайте настроим приложение под вашу ситуацию. Это займёт меньше минуты.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AlmostDoneContent(taxResidency: TaxResidency) {
    val countryEmoji = when (taxResidency) {
        TaxResidency.SPAIN -> "🇪🇸"
        TaxResidency.SERBIA -> "🇷🇸"
        TaxResidency.ARMENIA -> "🇦🇲"
        TaxResidency.RUSSIA -> "🇷🇺"
        else -> "🌍"
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = countryEmoji, style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Осталось совсем чуть-чуть",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Уточним несколько деталей о вашем налоговом статусе в ${taxResidency.label}. Это нужно для точных расчётов и напоминаний.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun <T> SelectionContent(
    title: String,
    subtitle: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))

        options.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    FilterChip(
                        selected = option == selected,
                        onClick = { onSelect(option) },
                        label = { Text(text = label(option), modifier = Modifier.padding(vertical = 8.dp)) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TextInputContent(
    title: String,
    subtitle: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Необязательно — можно заполнить позже в Настройках",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ToggleContent(
    title: String,
    subtitle: String,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SliderContent(
    title: String,
    subtitle: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    label: (Float) -> String,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = label(value),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${range.start.toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "${range.endInclusive.toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
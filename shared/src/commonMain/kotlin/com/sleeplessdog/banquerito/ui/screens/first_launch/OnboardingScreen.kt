package com.sleeplessdog.banquerito.ui.screens.first_launch


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.*
import com.sleeplessdog.banquerito.domain.model.ArmeniaEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.Citizenship
import com.sleeplessdog.banquerito.domain.model.Countries
import com.sleeplessdog.banquerito.domain.model.CountryOfResidence
import com.sleeplessdog.banquerito.domain.model.CountryTaxSettings
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.OnboardingSteps
import com.sleeplessdog.banquerito.domain.model.SerbiaEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.SpainAutonomoRegime
import com.sleeplessdog.banquerito.domain.model.SpainDeclarationType
import com.sleeplessdog.banquerito.domain.model.SpainEmploymentStatus
import com.sleeplessdog.banquerito.domain.model.TaxResidency
import com.sleeplessdog.banquerito.presentation.onboarding.OnboardingViewModel
import com.sleeplessdog.banquerito.ui.components.NumericKeypad
import com.sleeplessdog.banquerito.ui.components.WheelDatePicker
import com.sleeplessdog.banquerito.ui.icons.AppIcons
import com.sleeplessdog.banquerito.ui.screens.elements.BanqueritoBottomBar
import com.sleeplessdog.banquerito.ui.screens.elements.CountryPickerContent
import com.sleeplessdog.banquerito.ui.screens.elements.CurrencyPickerContent
import com.sleeplessdog.banquerito.ui.screens.elements.ListItemStatus
import com.sleeplessdog.banquerito.ui.screens.elements.ReminderConfig
import com.sleeplessdog.banquerito.ui.screens.elements.ReminderDefaults
import com.sleeplessdog.banquerito.ui.screens.elements.RemindersContent
import com.sleeplessdog.banquerito.ui.screens.elements.TextInputContent
import com.sleeplessdog.banquerito.ui.screens.elements.YearPickerContent
import com.sleeplessdog.banquerito.ui.util.BackHandler
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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

    BackHandler(enabled = state.step != OnboardingSteps.WELCOME) {
        viewModel.back()
    }

    val activeSteps = state.activeSteps()
    val progressSteps = activeSteps.filter { it != OnboardingSteps.WELCOME }
    val progressIndex = progressSteps.indexOf(state.step).coerceAtLeast(0)
    val progress = if (progressSteps.isEmpty()) 1f
    else (progressIndex + 1).toFloat() / progressSteps.size

    val isLastStep = activeSteps.indexOf(state.step) == activeSteps.lastIndex
    val buttonText = when {
        state.step == OnboardingSteps.ALMOST_DONE -> stringResource(Res.string.onboarding_btn_lets_go)
        isLastStep -> stringResource(Res.string.onboarding_btn_done)
        else -> stringResource(Res.string.onboarding_btn_next)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    val forward =
                        activeSteps.indexOf(targetState) > activeSteps.indexOf(initialState)
                    if (forward) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "onboarding_step",
                modifier = Modifier.weight(1f),
            ) { step ->
                val spain = state.countryTaxSettings as? CountryTaxSettings.Spain
                val serbia = state.countryTaxSettings as? CountryTaxSettings.Serbia
                val armenia = state.countryTaxSettings as? CountryTaxSettings.Armenia

                when (step) {

                    OnboardingSteps.WELCOME -> WelcomeContent()

                    OnboardingSteps.COUNTRY_OF_RESIDENCE -> CountryPickerContent(
                        title = stringResource(Res.string.onboarding_country_title),
                        searchPlaceholder = stringResource(Res.string.onboarding_country_search),
                        options = Countries.forResidence,
                        selected = state.selectedResidenceCountry,
                        onBack = viewModel::back,
                        onSelect = { viewModel.selectCountryOfResidence(it) },
                    )

                    OnboardingSteps.CITIZENSHIP -> CountryPickerContent(
                        title = stringResource(Res.string.onboarding_citizenship_title),
                        searchPlaceholder = stringResource(Res.string.onboarding_country_search),
                        options = Countries.forCitizenship,
                        selected = state.selectedCitizenshipCountry,
                        onBack = viewModel::back,
                        onSelect = { viewModel.selectCitizenship(it) },
                    )

                    OnboardingSteps.DEFAULT_CURRENCY -> CurrencyPickerContent(
                        title = stringResource(Res.string.onboarding_currency_title),
                        searchPlaceholder = stringResource(Res.string.onboarding_currency_search),
                        options = Countries.byCurrency,
                        selected = state.defaultCurrency,
                        onBack = viewModel::back,
                        onSelect = viewModel::selectCurrency,
                    )

                    OnboardingSteps.TAX_RESIDENCY -> CountryPickerContent(
                        title = stringResource(Res.string.onboarding_tax_residency_title),
                        searchPlaceholder = stringResource(Res.string.onboarding_country_search),
                        options = Countries.forTaxResidency,
                        selected = state.selectedTaxResidencyCountry,
                        onBack = viewModel::back,
                        onSelect = { viewModel.selectTaxResidency(it) },
                        infoRes = Res.string.onboarding_tax_residency_info,
                    )

                    OnboardingSteps.ALMOST_DONE -> AlmostDoneContent(
                        state.taxResidency ?: TaxResidency.OTHER
                    )

                    // ── Испания ───────────────────────────────────────────────

                    OnboardingSteps.SPAIN_STATUS -> SelectionContent(
                        title = stringResource(Res.string.onboarding_spain_status_title),
                        subtitle = stringResource(Res.string.onboarding_spain_status_subtitle),
                        options = SpainEmploymentStatus.entries,
                        selected = spain?.status ?: SpainEmploymentStatus.AUTONOMO,
                        label = { it.label },
                        onBack = viewModel::back,
                        onSelect = viewModel::updateSpainStatus,
                        infoRes = Res.string.onboarding_spain_status_info,
                    )

                    OnboardingSteps.SPAIN_REGIME -> SelectionContent(
                        title = stringResource(Res.string.onboarding_spain_regime_title),
                        subtitle = stringResource(Res.string.onboarding_spain_regime_subtitle),
                        options = SpainAutonomoRegime.entries,
                        selected = spain?.autonomoRegime ?: SpainAutonomoRegime.GENERAL,
                        label = { it.label },
                        onBack = viewModel::back,
                        onSelect = viewModel::updateSpainRegime,
                        infoRes = Res.string.onboarding_spain_regime_info,
                    )

                    OnboardingSteps.SPAIN_START_YEAR -> YearPickerContent(
                        title = stringResource(Res.string.onboarding_spain_start_year_title),
                        subtitle = stringResource(Res.string.onboarding_spain_start_year_subtitle),
                        value = spain?.autonomoStartYear,
                        onBack = viewModel::back,
                        onValueChange = { viewModel.updateSpainStartYear(it.toString()) },
                        infoRes = Res.string.onboarding_spain_start_year_info,
                    )

                    OnboardingSteps.SPAIN_IAE -> TextInputContent(
                        title = stringResource(Res.string.onboarding_spain_iae_title),
                        subtitle = stringResource(Res.string.onboarding_spain_iae_subtitle),
                        placeholder = stringResource(Res.string.onboarding_spain_iae_placeholder),
                        value = spain?.epigrafe ?: "",
                        onBack = viewModel::back,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                viewModel.updateSpainIae(input)
                            }
                        },
                        infoRes = Res.string.onboarding_spain_iae_info,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )

                    OnboardingSteps.SPAIN_IVA_PAYER -> ToggleContent(
                        title = stringResource(Res.string.onboarding_spain_iva_title),
                        subtitle = stringResource(Res.string.onboarding_spain_iva_subtitle),
                        label = stringResource(Res.string.onboarding_spain_iva_label),
                        checked = spain?.isIvaPayer ?: true,
                        onBack = viewModel::back,
                        onCheckedChange = viewModel::updateSpainIvaPayer,
                        infoRes = Res.string.onboarding_spain_iva_info,
                    )

                    OnboardingSteps.SPAIN_DECLARATION_TYPE -> SelectionContent(
                        title = stringResource(Res.string.onboarding_spain_declaration_title),
                        subtitle = stringResource(Res.string.onboarding_spain_declaration_subtitle),
                        options = SpainDeclarationType.entries,
                        selected = spain?.declarationType ?: SpainDeclarationType.MODELO_130,
                        label = { it.label },
                        onBack = viewModel::back,
                        onSelect = viewModel::updateSpainDeclarationType,
                        infoRes = Res.string.onboarding_spain_declaration_info,
                    )

                    OnboardingSteps.SPAIN_VISA_EXPIRY -> DatePickerContent(
                        title = stringResource(Res.string.onboarding_visa_expiry_title),
                        subtitle = stringResource(Res.string.onboarding_visa_expiry_subtitle),
                        value = spain?.visaExpiryDate,
                        onBack = viewModel::back,
                        onValueChange = viewModel::updateSpainVisaExpiry,
                    )

                    OnboardingSteps.SPAIN_TIE_EXPIRY -> DatePickerContent(
                        title = stringResource(Res.string.onboarding_spain_tie_expiry_title),
                        subtitle = stringResource(Res.string.onboarding_spain_tie_expiry_subtitle),
                        value = spain?.tieExpiryDate,
                        onBack = viewModel::back,
                        onValueChange = viewModel::updateSpainTieExpiry,
                        infoRes = Res.string.onboarding_spain_tie_info,
                    )

                    // ── Сербия ────────────────────────────────────────────────

                    OnboardingSteps.SERBIA_STATUS -> SelectionContent(
                        title = stringResource(Res.string.onboarding_serbia_status_title),
                        subtitle = stringResource(Res.string.onboarding_employment_status_subtitle),
                        options = SerbiaEmploymentStatus.entries,
                        selected = serbia?.status ?: SerbiaEmploymentStatus.SOLE_TRADER,
                        label = { it.label },
                        onBack = viewModel::back,
                        onSelect = viewModel::updateSerbiaStatus,
                    )

                    OnboardingSteps.SERBIA_PAUSHALNI -> ToggleContent(
                        title = stringResource(Res.string.onboarding_serbia_paushalni_title),
                        subtitle = stringResource(Res.string.onboarding_serbia_paushalni_subtitle),
                        label = stringResource(Res.string.onboarding_serbia_paushalni_label),
                        checked = serbia?.pausalniPorez ?: true,
                        onBack = viewModel::back,
                        onCheckedChange = viewModel::updateSerbiaPaushalni,
                        infoRes = Res.string.onboarding_serbia_paushalni_info,
                    )

                    OnboardingSteps.SERBIA_VAT -> ToggleContent(
                        title = stringResource(Res.string.onboarding_serbia_vat_title),
                        subtitle = stringResource(Res.string.onboarding_vat_subtitle),
                        label = stringResource(Res.string.onboarding_serbia_vat_label),
                        checked = serbia?.vatPayer ?: false,
                        onBack = viewModel::back,
                        onCheckedChange = viewModel::updateSerbiaVat,
                    )

                    OnboardingSteps.SERBIA_VISA_EXPIRY -> TextInputContent(
                        title = stringResource(Res.string.onboarding_visa_expiry_title),
                        subtitle = stringResource(Res.string.onboarding_visa_expiry_subtitle),
                        placeholder = stringResource(Res.string.onboarding_visa_expiry_placeholder),
                        value = serbia?.visaExpiryDate ?: "",
                        onBack = viewModel::back,
                        onValueChange = viewModel::updateSerbiaVisaExpiry,
                    )

                    // ── Армения ───────────────────────────────────────────────

                    OnboardingSteps.ARMENIA_STATUS -> SelectionContent(
                        title = stringResource(Res.string.onboarding_armenia_status_title),
                        subtitle = stringResource(Res.string.onboarding_employment_status_subtitle),
                        options = ArmeniaEmploymentStatus.entries,
                        selected = armenia?.status
                            ?: ArmeniaEmploymentStatus.INDIVIDUAL_ENTREPRENEUR,
                        label = { it.label },
                        onBack = viewModel::back,
                        onSelect = viewModel::updateArmeniaStatus,
                    )

                    OnboardingSteps.ARMENIA_IT_ZONE -> ToggleContent(
                        title = stringResource(Res.string.onboarding_armenia_it_zone_title),
                        subtitle = stringResource(Res.string.onboarding_armenia_it_zone_subtitle),
                        label = stringResource(Res.string.onboarding_armenia_it_zone_label),
                        checked = armenia?.itZone ?: false,
                        onBack = viewModel::back,
                        onCheckedChange = viewModel::updateArmeniaItZone,
                        infoRes = Res.string.onboarding_armenia_it_zone_info,
                    )

                    OnboardingSteps.ARMENIA_VAT -> ToggleContent(
                        title = stringResource(Res.string.onboarding_armenia_vat_title),
                        subtitle = stringResource(Res.string.onboarding_vat_subtitle),
                        label = stringResource(Res.string.onboarding_armenia_vat_label),
                        checked = armenia?.vatPayer ?: false,
                        onBack = viewModel::back,
                        onCheckedChange = viewModel::updateArmeniaVat,
                    )

                    OnboardingSteps.REMINDERS -> {
                        println("REMINDERS step, isValid: ${state.isCurrentStepValid}, isSaving: ${state.isSaving}")
                        val reminders = buildList {
                            // Виза — для Испании и Сербии
                            if (spain != null || serbia != null) {
                                add(ReminderConfig(
                                    titleRes = Res.string.onboarding_remind_visa_title,
                                    days = ReminderDefaults.VISA_DAYS,
                                    enabled = state.visaReminderEnabled,
                                    onToggle = viewModel::toggleVisaReminder,
                                ))
                            }
                            // Квартальная декларация — только Испания (autónomo)
                            if (spain?.status == SpainEmploymentStatus.AUTONOMO) {
                                add(
                                    ReminderConfig(
                                        titleRes = Res.string.onboarding_spain_remind_quarterly_title,
                                        enabled = state.quarterlyReminderEnabled,
                                        days = ReminderDefaults.QUARTERLY_DAYS,
                                        onToggle = viewModel::toggleQuarterlyReminder,
                                    )
                                )
                            }
                        }
                        RemindersContent(
                            reminders = reminders,
                            onBack = viewModel::back,
                        )
                    }
                }
            }

            if (state.step != OnboardingSteps.WELCOME) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                )
            }

            BanqueritoBottomBar(
                text = buttonText,
                onClick = viewModel::next,
                enabled = !state.isSaving && state.isCurrentStepValid,
                isLoading = state.isSaving,
            )
        }
    }
}

// ── Контент шагов ─────────────────────────────────────────────────────────────

@Composable
private fun WelcomeContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.onboarding_welcome_title),
            style = MaterialTheme.typography.displayLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.onboarding_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AlmostDoneContent(taxResidency: TaxResidency?) {
    val country = taxResidency?.let { Countries.byTaxResidency(it) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (country != null) {
            Image(
                painter = painterResource(country.flag),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(CircleShape),
            )
        } else {
            Text(text = "🌍", style = MaterialTheme.typography.displayLarge)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(
                Res.string.onboarding_almost_done_title,
                country?.let { stringResource(it.nameRes) }
                    ?: stringResource(Res.string.country_other),
            ),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(
                Res.string.onboarding_almost_done_subtitle,
                taxResidency?.label ?: stringResource(Res.string.country_other),
            ),
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
    onBack: () -> Unit,
    onSelect: (T) -> Unit,
    infoRes: StringResource? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        OnboardingStepHeader(title = title, onBack = onBack, infoRes = infoRes)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 56.dp, end = 24.dp),
        )
        Spacer(modifier = Modifier.height(32.dp))
        options.forEach { option ->
            ListItemStatus(
                option = label(option),
                selected = option == selected,
                onClick = { onSelect(option) },
            )

        }
        if (options.size == 1) Spacer(modifier = Modifier.weight(1f))
    }

}


@Composable
private fun ToggleContent(
    title: String,
    subtitle: String,
    label: String,
    checked: Boolean,
    onBack: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    infoRes: StringResource? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
    ) {
        OnboardingStepHeader(title = title, onBack = onBack, infoRes = infoRes)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 54.dp, end = 24.dp),
        )
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Color(0xFF379310),
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

@Composable
private fun SliderContent(
    title: String,
    subtitle: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    label: @Composable (Float) -> String,
    onBack: () -> Unit,
    onValueChange: (Float) -> Unit,
    infoRes: StringResource? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
    ) {
        OnboardingStepHeader(title = title, onBack = onBack, infoRes = infoRes)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = label(value),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${range.start.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${range.endInclusive.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DatePickerContent(
    title: String,
    subtitle: String,
    value: String?,
    onBack: () -> Unit,
    onValueChange: (String) -> Unit,
    infoRes: StringResource? = null,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingStepHeader(title = title, onBack = onBack, infoRes = infoRes)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 56.dp, end = 24.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        WheelDatePicker(
            selected = value,
            onDateSelected = onValueChange,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

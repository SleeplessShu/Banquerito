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
import androidx.compose.material3.Scaffold
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
import com.sleeplessdog.banquerito.domain.model.Citizenship
import com.sleeplessdog.banquerito.domain.model.CountryOfResidence
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.TaxResidency
import com.sleeplessdog.banquerito.presentation.onboarding.ONBOARDING_SELECTION_STEPS
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
            // Прогресс-бар (только на шагах выбора)
            if (state.step != OnboardingStep.WELCOME) {
                val progress = state.step.index.toFloat() / ONBOARDING_SELECTION_STEPS
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
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
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStepContent()
                    OnboardingStep.COUNTRY_OF_RESIDENCE -> SelectionStepContent(
                        title = "Страна проживания",
                        subtitle = "Где вы сейчас живёте?",
                        options = CountryOfResidence.entries,
                        selected = state.countryOfResidence,
                        label = { it.label },
                        onSelect = viewModel::selectCountryOfResidence,
                    )
                    OnboardingStep.CITIZENSHIP -> SelectionStepContent(
                        title = "Гражданство",
                        subtitle = "Гражданином какой страны вы являетесь?",
                        options = Citizenship.entries,
                        selected = state.citizenship,
                        label = { it.label },
                        onSelect = viewModel::selectCitizenship,
                    )
                    OnboardingStep.DEFAULT_CURRENCY -> SelectionStepContent(
                        title = "Валюта по умолчанию",
                        subtitle = "В какой валюте вы ведёте основные расчёты?",
                        options = Currency.entries,
                        selected = state.defaultCurrency,
                        label = { it.code },
                        onSelect = viewModel::selectCurrency,
                    )
                    OnboardingStep.TAX_RESIDENCY -> SelectionStepContent(
                        title = "Налоговое резидентство",
                        subtitle = "В какой стране вы являетесь налоговым резидентом?",
                        options = TaxResidency.entries,
                        selected = state.taxResidency,
                        label = { it.label },
                        onSelect = viewModel::selectTaxResidency,
                    )
                }
            }

            // Нижняя кнопка
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
                    Text(
                        text = if (state.step == OnboardingStep.TAX_RESIDENCY) "Готово" else "Далее",
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStepContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "👋",
            style = MaterialTheme.typography.displayLarge,
        )
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
private fun <T> SelectionStepContent(
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
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Чипы с вариантами — в строки по 2
        val chunked = options.chunked(2)
        chunked.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { option ->
                    FilterChip(
                        selected = option == selected,
                        onClick = { onSelect(option) },
                        label = {
                            Text(
                                text = label(option),
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
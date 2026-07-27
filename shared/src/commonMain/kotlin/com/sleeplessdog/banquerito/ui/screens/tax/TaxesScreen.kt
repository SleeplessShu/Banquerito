package com.sleeplessdog.banquerito.ui.screens.tax


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.presentation.taxes.IncomeSlider
import com.sleeplessdog.banquerito.presentation.taxes.TaxBreakdownBar
import com.sleeplessdog.banquerito.presentation.taxes.TaxesViewModel
import com.sleeplessdog.banquerito.presentation.taxes.UpcomingDeadlines
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TaxesScreen(
    viewModel: TaxesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Налоги",
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }

        item {
            IncomeSlider(
                income = uiState.effectiveIncome,
                currency = uiState.currency,
                isCustom = uiState.sliderIncome != null,
                onIncomeChange = { viewModel.updateSliderIncome(it) },
                onReset = { viewModel.resetSliderToActual() },
            )
        }

        uiState.calculation?.let { calc ->
            item {
                TaxBreakdownBar(
                    segments = calc.segments,
                    currency = calc.currency,
                )
            }
        }

        item {
            UpcomingDeadlines(deadlines = uiState.deadlines)
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
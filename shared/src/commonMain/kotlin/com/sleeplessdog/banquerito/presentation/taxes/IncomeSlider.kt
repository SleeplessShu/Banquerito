package com.sleeplessdog.banquerito.presentation.taxes


import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.ui.screens.accounts.formatAmount

@Composable
fun IncomeSlider(
    income: Double,
    currency: Currency,
    isCustom: Boolean,
    onIncomeChange: (Double) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxIncome = (income * 2).coerceAtLeast(1000.0)

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Доход за период",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isCustom) {
                Text(
                    text = "Сбросить",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickableText { onReset() }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatAmount(income, currency),
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = income.toFloat(),
            onValueChange = { onIncomeChange(it.toDouble()) },
            valueRange = 0f..maxIncome.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
            )
        )
    }
}

private fun Modifier.clickableText(onClick: () -> Unit): Modifier =
    this.then(
        Modifier
    )
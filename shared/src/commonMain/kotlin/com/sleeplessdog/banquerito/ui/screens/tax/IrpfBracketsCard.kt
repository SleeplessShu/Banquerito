package com.sleeplessdog.banquerito.ui.screens.tax

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.IncomeExtrapolationMode
import com.sleeplessdog.banquerito.domain.model.IrpfBreakdown
import com.sleeplessdog.banquerito.ui.screens.accounts.formatAmount

@Composable
fun IrpfBracketsCard(
    breakdown: IrpfBreakdown,
    currency: Currency,
    mode: IncomeExtrapolationMode,
    onModeChange: (IncomeExtrapolationMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

        Text(
            text = "Прогрессивный IRPF",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        // переключатель режима
        Column {
            IncomeExtrapolationMode.entries.forEach { m ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = mode == m,
                        onClick = { onModeChange(m) }
                    )
                    Text(
                        text = m.label,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Годовой доход для расчёта: ${formatAmount(breakdown.annualIncomeUsed, currency)}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Эффективная ставка: ${(breakdown.effectiveRate * 100).toInt()}%",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // разбивка по траншам
        breakdown.bracketAmounts.forEach { bracketAmount ->
            val isCurrent = bracketAmount.bracket == breakdown.currentBracket
            Surface(
                color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${formatAmount(bracketAmount.bracket.from, currency)} – ${
                                if (bracketAmount.bracket.to == Double.MAX_VALUE) "∞"
                                else formatAmount(bracketAmount.bracket.to, currency)
                            }",
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(bracketAmount.bracket.rate * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = formatAmount(bracketAmount.taxPaid, currency),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Итого IRPF за год",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatAmount(breakdown.totalTax, currency),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                breakdown.amountToNextBracketMonthly?.let { amount ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "До следующего транша можно зарабатывать ещё ${formatAmount(amount, currency)}/мес",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
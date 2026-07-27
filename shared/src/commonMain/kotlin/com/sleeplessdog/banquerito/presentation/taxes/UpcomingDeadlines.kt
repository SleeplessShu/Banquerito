package com.sleeplessdog.banquerito.presentation.taxes


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.domain.model.TaxDeadline
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun UpcomingDeadlines(
    deadlines: List<TaxDeadline>,
    modifier: Modifier = Modifier,
) {
    if (deadlines.isEmpty()) return

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Ближайшие дедлайны",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        deadlines.forEach { deadline ->
            val daysUntil = (deadline.date.toEpochDays() - today.toEpochDays()).toInt()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
                    Column {
                        Text(
                            text = deadline.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${deadline.date.dayOfMonth}.${deadline.date.monthNumber.toString().padStart(2, '0')}.${deadline.date.year}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = when {
                            daysUntil == 0 -> "Сегодня"
                            daysUntil == 1 -> "Завтра"
                            daysUntil < 0 -> "Просрочено"
                            else -> "Через $daysUntil дней"
                        },
                        fontSize = 12.sp,
                        color = if (daysUntil <= 7) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
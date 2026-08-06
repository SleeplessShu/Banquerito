package com.sleeplessdog.banquerito.presentation.taxes


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.domain.model.TaxSegment
import com.sleeplessdog.banquerito.domain.model.TaxSegmentColor
import com.sleeplessdog.banquerito.ui.BanqueritoColors
import com.sleeplessdog.banquerito.ui.screens.accounts.formatAmount

@Composable
fun TaxBreakdownBar(
    segments: List<TaxSegment>,
    currency: Currency,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

        // сама полоса — сегменты, а внутри IRPF ещё и подсегменты-оттенки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            segments.forEach { segment ->
                if (segment.percentOfGross <= 0f) return@forEach

                if (segment.subSegments.isNotEmpty()) {
                    Row(modifier = Modifier.weight(segment.percentOfGross.coerceAtLeast(0.001f)).fillMaxHeight()) {
                        segment.subSegments.forEachIndexed { index, sub ->
                            if (sub.percentOfGross <= 0f) return@forEachIndexed
                            Surface(
                                color = shadeFor(segment.colorRole, index, segment.subSegments.size),
                                modifier = Modifier
                                    .weight(sub.percentOfGross.coerceAtLeast(0.001f))
                                    .fillMaxHeight()
                            ) {}
                        }
                    }
                } else {
                    Surface(
                        color = colorFor(segment.colorRole),
                        modifier = Modifier
                            .weight(segment.percentOfGross.coerceAtLeast(0.001f))
                            .fillMaxHeight()
                    ) {}
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        segments.forEach { segment ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = colorFor(segment.colorRole),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = segment.label,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatAmount(segment.amount, currency),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(segment.percentOfGross * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // подсегменты — с отступом, показываем только если их больше одного
            if (segment.subSegments.size > 1) {
                segment.subSegments.forEachIndexed { index, sub ->
                    if (sub.amount <= 0.0) return@forEachIndexed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = shadeFor(segment.colorRole, index, segment.subSegments.size),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.size(7.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = sub.label,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatAmount(sub.amount, currency),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun colorFor(role: TaxSegmentColor): Color = when (role) {
    TaxSegmentColor.NET_INCOME -> BanqueritoColors.TaxNetIncome
    TaxSegmentColor.IRPF -> BanqueritoColors.TaxIrpf
    TaxSegmentColor.IVA -> BanqueritoColors.TaxIva
    TaxSegmentColor.CUOTA -> BanqueritoColors.TaxCuota
    TaxSegmentColor.OTHER -> BanqueritoColors.TaxOther
}

private fun shadeFor(role: TaxSegmentColor, index: Int, total: Int): Color {
    val base = colorFor(role)
    if (total <= 1) return base
    val minAlpha = 0.45f
    val step = (1f - minAlpha) / (total - 1).coerceAtLeast(1)
    val alpha = (minAlpha + step * index).coerceIn(minAlpha, 1f)
    return base.copy(alpha = alpha)
}
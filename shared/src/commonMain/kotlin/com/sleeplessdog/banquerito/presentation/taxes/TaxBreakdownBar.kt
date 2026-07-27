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

        // сама полоса Row с weight по процентам
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            segments.forEach { segment ->
                if (segment.percentOfGross > 0f) {
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

        // подписи под полосой
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
        }
    }
}

private fun colorFor(role: TaxSegmentColor) = when (role) {
    TaxSegmentColor.NET_INCOME -> BanqueritoColors.Success
    TaxSegmentColor.IRPF -> BanqueritoColors.Error
    TaxSegmentColor.IVA -> BanqueritoColors.PrimaryContainer
    TaxSegmentColor.CUOTA -> BanqueritoColors.Primary
    TaxSegmentColor.OTHER -> BanqueritoColors.OnSurfaceVariant
}
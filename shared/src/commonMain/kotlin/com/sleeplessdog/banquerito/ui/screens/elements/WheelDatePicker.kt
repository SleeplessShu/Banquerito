package com.sleeplessdog.banquerito.ui.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.abs

private val ITEM_HEIGHT = 56.dp
private val VISIBLE_ITEMS = 5

private fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)

private fun daysInMonth(month: Int, year: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

@Composable
fun WheelDatePicker(
    selected: String?,          // формат "dd.MM.yyyy"
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    val initialDay   = selected?.split(".")?.getOrNull(0)?.toIntOrNull() ?: today.dayOfMonth
    val initialMonth = selected?.split(".")?.getOrNull(1)?.toIntOrNull() ?: today.monthNumber
    val initialYear  = selected?.split(".")?.getOrNull(2)?.toIntOrNull() ?: today.year

    var selectedDay   by remember { mutableIntStateOf(initialDay) }
    var selectedMonth by remember { mutableIntStateOf(initialMonth) }
    var selectedYear  by remember { mutableIntStateOf(initialYear) }

    val years  = remember { (today.year..today.year + 20).toList() }
    val months = remember { (1..12).toList() }

    val days by remember {
        derivedStateOf {
            (1..daysInMonth(selectedMonth, selectedYear)).toList()
        }
    }

    LaunchedEffect(days) {
        if (selectedDay > days.size) selectedDay = days.size
    }

    // Уведомляем об изменении даты
    LaunchedEffect(selectedDay, selectedMonth, selectedYear) {
        val d = selectedDay.toString().padStart(2, '0')
        val m = selectedMonth.toString().padStart(2, '0')
        onDateSelected("$d.$m.$selectedYear")
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            WheelColumn(
                items = days,
                initialIndex = (days.indexOf(selectedDay)).coerceAtLeast(0),
                label = { it.toString().padStart(2, '0') },
                onSelected = { selectedDay = it },
                modifier = Modifier.width(72.dp),
            )

            Text(
                text = ".",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 2.dp),
            )

            WheelColumn(
                items = months,
                initialIndex = (months.indexOf(selectedMonth)).coerceAtLeast(0),
                label = { it.toString().padStart(2, '0') },
                onSelected = { selectedMonth = it },
                modifier = Modifier.width(72.dp),
            )

            Text(
                text = ".",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 2.dp),
            )

            WheelColumn(
                items = years,
                initialIndex = (years.indexOf(selectedYear)).coerceAtLeast(0),
                label = { it.toString() },
                onSelected = { selectedYear = it },
                modifier = Modifier.width(100.dp),
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = ITEM_HEIGHT)
                .fillMaxWidth(0.85f),
            color = MaterialTheme.colorScheme.primaryContainer,
            thickness = 1.dp,
        )
        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = ITEM_HEIGHT)
                .fillMaxWidth(0.85f),
            color = MaterialTheme.colorScheme.primaryContainer,
            thickness = 1.dp,
        )
    }
}

@Composable
private fun <T> WheelColumn(
    items: List<T>,
    initialIndex: Int,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = VISIBLE_ITEMS / 2
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialIndex - padding).coerceAtLeast(0),
    )
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val centeredIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val center = info.viewportStartOffset +
                    (info.viewportEndOffset - info.viewportStartOffset) / 2
            info.visibleItemsInfo
                .minByOrNull { abs((it.offset + it.size / 2) - center) }
                ?.index
                ?.minus(padding)
        }
    }

    LaunchedEffect(centeredIndex) {
        centeredIndex?.let { idx ->
            items.getOrNull(idx)?.let { onSelected(it) }
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = snapBehavior,
        modifier = modifier.height(ITEM_HEIGHT * VISIBLE_ITEMS),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(padding) {
            Box(modifier = Modifier.height(ITEM_HEIGHT))
        }

        itemsIndexed(items) { index, item ->
            val distanceFromCenter by remember {
                derivedStateOf {
                    val info = listState.layoutInfo
                    val center = info.viewportStartOffset +
                            (info.viewportEndOffset - info.viewportStartOffset) / 2
                    val itemInfo = info.visibleItemsInfo.find { it.index == index + padding }
                    if (itemInfo != null) {
                        abs((itemInfo.offset + itemInfo.size / 2) - center)
                            .toFloat() / (ITEM_HEIGHT.value * 3)
                    } else 1f
                }
            }

            val alpha    = (1f - distanceFromCenter * 0.8f).coerceIn(0.2f, 1f)
            val scale    = (1f - distanceFromCenter * 0.15f).coerceIn(0.8f, 1f)
            val isCenter = distanceFromCenter < 0.1f

            Box(
                modifier = Modifier
                    .height(ITEM_HEIGHT)
                    .graphicsLayer {
                        this.alpha = alpha
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(item),
                    style = if (isCenter) MaterialTheme.typography.headlineSmall
                    else MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCenter) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        items(padding) {
            Box(modifier = Modifier.height(ITEM_HEIGHT))
        }
    }
}
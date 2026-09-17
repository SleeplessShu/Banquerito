package com.sleeplessdog.banquerito.ui.screens.elements


import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
private val ITEM_HEIGHT = 56.dp
private val VISIBLE_ITEMS = 5  // нечётное — центральный видимый

@Composable
fun WheelYearPicker(
    selected: Int?,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentYear = remember {
        Clock.System.todayIn(TimeZone.currentSystemDefault()).year
    }
    val years = remember { (currentYear downTo currentYear - 20).toList() }

    val initialIndex = remember(selected) {
        if (selected != null) years.indexOf(selected).coerceAtLeast(0) else 0
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialIndex - VISIBLE_ITEMS / 2).coerceAtLeast(0),
    )
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Определяем центральный элемент и сообщаем наружу
    val centeredIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset +
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportCenter)
            }?.index
        }
    }

    LaunchedEffect(centeredIndex) {
        centeredIndex?.let { idx ->
            val year = years.getOrNull(idx)
            if (year != null) onYearSelected(year)
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .height(ITEM_HEIGHT * VISIBLE_ITEMS),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Отступы сверху и снизу чтобы крайние элементы центрировались
            val padding = VISIBLE_ITEMS / 2
            items(padding) { Box(modifier = Modifier.height(ITEM_HEIGHT)) }

            itemsIndexed(years) { index, year ->
                val distanceFromCenter by remember {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val viewportCenter = layoutInfo.viewportStartOffset +
                                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                        val itemInfo = layoutInfo.visibleItemsInfo.find {
                            it.index == index + padding
                        }
                        if (itemInfo != null) {
                            abs((itemInfo.offset + itemInfo.size / 2) - viewportCenter)
                                .toFloat() / (ITEM_HEIGHT.value * 3)
                        } else 1f
                    }
                }

                val alpha = (1f - distanceFromCenter * 0.8f).coerceIn(0.2f, 1f)
                val scale = (1f - distanceFromCenter * 0.15f).coerceIn(0.8f, 1f)
                val isCenter = distanceFromCenter < 0.1f

                Box(
                    modifier = Modifier
                        .height(ITEM_HEIGHT)
                        .fillMaxWidth()
                        .graphicsLayer {
                            this.alpha = alpha
                            scaleX = scale
                            scaleY = scale
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = year.toString(),
                        style = if (isCenter) MaterialTheme.typography.headlineSmall
                        else MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCenter) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            items(padding) { Box(modifier = Modifier.height(ITEM_HEIGHT)) }
        }

        // Линии выделения центрального элемента
        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = ITEM_HEIGHT)
                .fillMaxWidth(0.6f),
            color = MaterialTheme.colorScheme.primaryContainer,
            thickness = 1.dp,
        )
        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = ITEM_HEIGHT)
                .fillMaxWidth(0.6f),
            color = MaterialTheme.colorScheme.primaryContainer,
            thickness = 1.dp,
        )
    }
}

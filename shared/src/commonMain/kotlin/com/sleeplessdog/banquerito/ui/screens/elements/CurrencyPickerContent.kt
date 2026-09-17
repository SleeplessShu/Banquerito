package com.sleeplessdog.banquerito.ui.screens.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.sleeplessdog.banquerito.domain.model.Country
import com.sleeplessdog.banquerito.domain.model.Currency
import com.sleeplessdog.banquerito.ui.icons.AppIcons
import org.jetbrains.compose.resources.stringResource

@Composable
fun CurrencyPickerContent(
    title: String,
    searchPlaceholder: String,
    options: List<Country>,
    selected: Currency?,
    onBack: () -> Unit,
    onSelect: (Currency) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filtered = if (query.isBlank()) options
    else options.filter { country ->
        val currencyName = stringResource(country.currency.nameRes)
        country.currency.code.contains(query, ignoreCase = true) ||
                currencyName.contains(query, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Заголовок ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = AppIcons.arrowBack(),
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        // ── Поиск ─────────────────────────────────────────────────────────────
        BanqueritoSearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = searchPlaceholder,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )

        // ── Список ────────────────────────────────────────────────────────────
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { it.currency.code }) { country ->
                CurrencyListItem(
                    country = country,
                    selected = country.currency == selected,
                    onClick = {
                        focusManager.clearFocus()
                        onSelect(country.currency)
                    },
                )
            }
        }
    }
}
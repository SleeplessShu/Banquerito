package com.sleeplessdog.banquerito.ui.screens.elements


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.action_close
import com.sleeplessdog.banquerito.domain.model.Country
import com.sleeplessdog.banquerito.ui.icons.AppIcons
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CountryPickerContent(
    title: String,
    searchPlaceholder: String,
    options: List<Country>,
    selected: Country?,
    onBack: () -> Unit,
    onSelect: (Country) -> Unit,
    infoRes: StringResource? = null,
) {
    val focusManager = LocalFocusManager.current

    var query by remember { mutableStateOf("") }
    var showInfo by remember { mutableStateOf(false) }

    val resolvedOptions = remember(options) {
        options.map { it to it.nameRes }
    }

    val filtered = if (query.isBlank()) {
        options
    } else {
        resolvedOptions
            .filter { (_, nameRes) ->
                stringResource(nameRes).contains(query, ignoreCase = true)
            }
            .map { (country, _) -> country }
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
            if (infoRes != null) {
                IconButton(onClick = { showInfo = true }) {
                    Icon(
                        painter = AppIcons.info(),
                        contentDescription = "Подробнее",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // ── Поиск ─────────────────────────────────────────────────────────────
        BanqueritoSearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = searchPlaceholder,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Список ────────────────────────────────────────────────────────────
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { it.code }) { country ->
                SelectableListItem(
                    country = country,
                    selected = country == selected,
                    onClick = {
                        focusManager.clearFocus()
                        onSelect(country) },
                )
            }
        }
    }

    if (showInfo && infoRes != null) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            text = {
                Text(
                    text = stringResource(infoRes),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text(stringResource(Res.string.action_close))
                }
            },
        )
    }
}
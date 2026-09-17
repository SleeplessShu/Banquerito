package com.sleeplessdog.banquerito.ui.screens.first_launch

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.action_close
import com.sleeplessdog.banquerito.ui.icons.AppIcons
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun OnboardingStepHeader(
    title: String,
    onBack: () -> Unit,
    infoRes: StringResource? = null,
) {
    var showInfo by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 8.dp),
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

    if (showInfo && infoRes != null) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            shape = RoundedCornerShape(8.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            text = {
                Text(
                    text = stringResource(infoRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text(
                        text = stringResource(Res.string.action_close),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )
    }
}
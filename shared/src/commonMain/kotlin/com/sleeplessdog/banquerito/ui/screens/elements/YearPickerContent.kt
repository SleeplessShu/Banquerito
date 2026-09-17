package com.sleeplessdog.banquerito.ui.screens.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import com.sleeplessdog.banquerito.ui.screens.first_launch.OnboardingStepHeader
@Composable
fun YearPickerContent(
    title: String,
    subtitle: String,
    value: Int?,
    onBack: () -> Unit,
    onValueChange: (Int) -> Unit,
    infoRes: StringResource? = null,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingStepHeader(title = title, onBack = onBack, infoRes = infoRes)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 56.dp, end = 24.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        WheelYearPicker(
            selected = value,
            onYearSelected = onValueChange,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

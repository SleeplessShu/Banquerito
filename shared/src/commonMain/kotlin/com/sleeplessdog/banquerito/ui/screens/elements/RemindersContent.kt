package com.sleeplessdog.banquerito.ui.screens.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.onboarding_remind_visa_title
import banquerito.shared.generated.resources.onboarding_reminders_subtitle
import banquerito.shared.generated.resources.onboarding_reminders_title
import banquerito.shared.generated.resources.onboarding_spain_remind_quarterly_title
import com.sleeplessdog.banquerito.ui.screens.first_launch.OnboardingStepHeader
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


data class ReminderConfig(
    val titleRes: StringResource,
    val days: Int,
    val enabled: Boolean,
    val onToggle: (Boolean) -> Unit,
)

object ReminderDefaults {
    const val VISA_DAYS = 180
    const val QUARTERLY_DAYS = 30
}

@Composable
fun RemindersContent(
    reminders: List<ReminderConfig>,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        OnboardingStepHeader(
            title = stringResource(Res.string.onboarding_reminders_title),
            onBack = onBack,
        )
        Text(
            text = stringResource(Res.string.onboarding_reminders_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 56.dp, end = 24.dp),
        )
        Spacer(modifier = Modifier.height(32.dp))

        reminders.forEachIndexed { index, config ->
            ReminderItem(config = config)
            if (index < reminders.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            }
        }
    }
}
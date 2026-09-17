package com.sleeplessdog.banquerito.ui.screens.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.onboarding_optional_hint
import com.sleeplessdog.banquerito.ui.components.NumericKeypad
import com.sleeplessdog.banquerito.ui.screens.first_launch.OnboardingStepHeader
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun TextInputContent(
    title: String,
    subtitle: String,
    placeholder: String,
    value: String,
    onBack: () -> Unit,
    onValueChange: (String) -> Unit,
    infoRes: StringResource? = null,
    numericKeypad: Boolean = false,
    maxLength: Int = Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var keypadVisible by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (keypadVisible) 240.dp else 24.dp),
        ) {
            OnboardingStepHeader(title = title, onBack = onBack, infoRes = infoRes)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 54.dp, end = 24.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                keyboardOptions = keyboardOptions,
                value = value,
                onValueChange = { if (!numericKeypad) onValueChange(it) },
                placeholder = {
                    Text(text = placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                singleLine = true,
                readOnly = numericKeypad,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused && numericKeypad) {
                            keyboardController?.hide()
                            keypadVisible = true
                        }
                    },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.onboarding_optional_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }

        if (numericKeypad) {
            NumericKeypad(
                visible = keypadVisible,
                value = value,
                onValueChange = onValueChange,
                onDone = { keypadVisible = false },
                maxLength = maxLength,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
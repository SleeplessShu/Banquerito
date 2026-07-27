package com.sleeplessdog.banquerito.presentation.consultant

import androidx.compose.runtime.Composable

expect class FilePickerLauncher {
    fun launch()
}

@Composable
expect fun rememberFilePickerLauncher(
    onFilePicked: (name: String, mimeType: String, bytes: ByteArray) -> Unit,
    onError: () -> Unit,
): FilePickerLauncher
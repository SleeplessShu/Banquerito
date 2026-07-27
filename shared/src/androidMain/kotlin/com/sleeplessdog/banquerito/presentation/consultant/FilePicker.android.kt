package com.sleeplessdog.banquerito.presentation.consultant

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class FilePickerLauncher(
    private val launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    actual fun launch() {
        launcher.launch(arrayOf(
            "application/pdf",
            "image/*",
            "text/plain",
            "text/csv",
            "application/rtf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",))
    }
}

@Composable
actual fun rememberFilePickerLauncher(
    onFilePicked: (name: String, mimeType: String, bytes: ByteArray) -> Unit,
    onError: () -> Unit,
): FilePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            onError()
            return@rememberLauncherForActivityResult
        }
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val name = getFileName(context, uri) ?: "file"
            if (bytes != null) {
                onFilePicked(name, mimeType, bytes)
            } else {
                onError()
            }
        } catch (e: Exception) {
            onError()
        }
    }
    return FilePickerLauncher(launcher)
}

private fun getFileName(context: Context, uri: android.net.Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}
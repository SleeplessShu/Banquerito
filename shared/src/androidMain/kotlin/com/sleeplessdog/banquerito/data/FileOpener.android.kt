package com.sleeplessdog.banquerito.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

actual class FileOpener(private val context: Context) {
    actual fun openFile(path: String, mimeType: String) {
        val file = File(path)
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}
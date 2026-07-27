package com.sleeplessdog.banquerito.data

import android.content.Context
import java.io.File

actual class FileStorage(private val context: Context) {
    actual suspend fun saveFile(name: String, bytes: ByteArray): String {
        val dir = File(context.filesDir, "chat_attachments")
        if (!dir.exists()) dir.mkdirs()
        val safeName = "${System.currentTimeMillis()}_$name"
        val file = File(dir, safeName)
        file.writeBytes(bytes)
        return file.absolutePath
    }

    actual suspend fun readFile(path: String): ByteArray? {
        val file = File(path)
        return if (file.exists()) file.readBytes() else null
    }
}
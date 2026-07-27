package com.sleeplessdog.banquerito.data

expect object DocumentTextExtractor {
    fun extractText(bytes: ByteArray, mimeType: String, fileName: String): String?
}

fun isTextExtractable(mimeType: String, fileName: String): Boolean {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return mimeType.startsWith("text/") ||
            mimeType == "application/rtf" ||
            mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
            mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
            mimeType == "text/csv" ||
            ext in listOf("txt", "csv", "rtf", "docx", "xlsx")
}
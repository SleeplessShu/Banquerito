package com.sleeplessdog.banquerito.data

expect class FileOpener {
    fun openFile(path: String, mimeType: String)
}
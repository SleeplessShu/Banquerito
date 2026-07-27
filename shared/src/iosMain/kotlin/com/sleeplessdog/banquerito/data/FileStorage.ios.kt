package com.sleeplessdog.banquerito.data

actual class FileStorage {
    actual suspend fun saveFile(name: String, bytes: ByteArray): String {
        TODO("iOS implementation")
    }
    actual suspend fun readFile(path: String): ByteArray? {
        TODO("iOS implementation")
    }
}
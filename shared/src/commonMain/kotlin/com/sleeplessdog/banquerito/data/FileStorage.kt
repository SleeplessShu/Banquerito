package com.sleeplessdog.banquerito.data

expect class FileStorage {
    suspend fun saveFile(name: String, bytes: ByteArray): String
    suspend fun readFile(path: String): ByteArray?
}
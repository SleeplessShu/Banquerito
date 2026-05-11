package com.sleeplessdog.banquerito.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.sleeplessdog.banquerito.db.BanqueritoDB

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(BanqueritoDB.Schema, context, "banquerito.db")
}
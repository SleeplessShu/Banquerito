package com.sleeplessdog.banquerito.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.sleeplessdog.banquerito.db.BanqueritoDB

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(BanqueritoDB.Schema, "banquerito.db")
}
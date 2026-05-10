package com.sleeplessdog.banquerito

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
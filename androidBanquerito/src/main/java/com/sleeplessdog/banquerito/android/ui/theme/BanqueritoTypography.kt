package com.sleeplessdog.banquerito.android.ui.theme


import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sleeplessdog.banquerito.ui.theme.onest
import com.sleeplessdog.banquerito.ui.theme.sourceSerif4

@Composable
fun banqueritoTypography(): Typography {
    val serif = sourceSerif4()
    val sans  = onest()

    return Typography(
        // heading/h1 — 40sp, SemiBold, ls -1.5%
        displayLarge = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            letterSpacing = (-0.6).sp,
        ),
        // heading/h2 — 24sp, SemiBold, ls -0.5%
        headlineMedium = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.12).sp,
        ),
        // heading/h3 — 20sp, SemiBold, ls -0.5%
        headlineSmall = TextStyle(
            fontFamily = serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.1).sp,
        ),
        // paragraph/medium — 16sp, Medium
        bodyLarge = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        // paragraph/light — 16sp, Light
        bodyMedium = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Light,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        // caption/medium — 14sp, Medium, ls +0.5%
        labelLarge = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.07.sp,
        ),
        // caption/light — 14sp, Light, ls +0.5%
        labelMedium = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Light,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.07.sp,
        ),
        // caption/nav-bar — 11sp, Medium, ls +1%
        labelSmall = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.11.sp,
        ),
    )
}

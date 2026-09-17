package com.sleeplessdog.banquerito.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.*
import org.jetbrains.compose.resources.Font

@Composable
fun sourceSerif4(): FontFamily = FontFamily(
    Font(Res.font.SourceSerif4_SemiBold, FontWeight.SemiBold),
)

@Composable
fun onest(): FontFamily = FontFamily(
    Font(Res.font.Onest_Light, FontWeight.Light),
    Font(Res.font.Onest_Medium, FontWeight.Medium),
)
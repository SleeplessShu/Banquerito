package com.sleeplessdog.banquerito.android

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.sleeplessdog.banquerito.android.ui.theme.banqueritoTypography
import com.sleeplessdog.banquerito.ui.BanqueritoColorScheme

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {

    val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(0.dp)
    )

    MaterialTheme(
        colorScheme = BanqueritoColorScheme,
        typography = banqueritoTypography(),
        shapes = shapes,
        content = content
    )
}

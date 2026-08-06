package com.sleeplessdog.banquerito.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.*
import com.sleeplessdog.banquerito.ui.icons.AppIcons
import org.jetbrains.compose.resources.stringResource

data class BottomNavItem(
    val route: String,
    val icon: @Composable () -> Unit,
)


@Composable
fun bottomNavItems(currentRoute: String?) = listOf(
    BottomNavItem("accounts") {
        BottomNavIconWithLabel(
            icon = AppIcons.bank(),
            label = "Счета",
            isSelected = currentRoute == "accounts",
        )
    },

    BottomNavItem("operations") {
        BottomNavIconWithLabel(
            icon = AppIcons.strategy(),
            label = "Планы",
            isSelected = currentRoute == "operations",
        )
    },

    BottomNavItem("taxes") {
        BottomNavIconWithLabel(
            icon = AppIcons.wallet(),
            label = "Налоги",
            isSelected = currentRoute == "taxes",
        )
    },
    BottomNavItem("consultant") {
        BottomNavIconWithLabel(
            icon = AppIcons.agent(),
            label = "Ассистент",
            isSelected = currentRoute == "consultant",
        )
    },
    BottomNavItem("settings") {
        BottomNavIconWithLabel(
            icon = AppIcons.settings(),
            label = "Настройки",
            isSelected = currentRoute == "settings",
        )
    },
)
@Composable
fun BottomNavIconWithLabel(
    icon: Painter,
    label: String,
    isSelected: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.surfaceTint
                    else Color.Transparent
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {Icon(
            painter = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimary,
        )}
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
        )
    }
}

@Composable
fun BottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        val items = bottomNavItems(currentRoute)
        items.forEach { item ->
            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .weight(1f),

                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true),
                        ) {
                            navController.navigate(item.route) {
                                popUpTo("accounts") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    item.icon()
                }
            }
        }
    }
}



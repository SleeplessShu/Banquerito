package com.sleeplessdog.banquerito.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
    BottomNavItem("operations") {
        Icon(
            painter = AppIcons.strategy(),
            contentDescription = null,
            modifier = if (currentRoute == "operations")  Modifier.size(36.dp)
            else Modifier.size(28.dp),
        )
    },
    BottomNavItem("accounts") {
        Icon(
            painter = AppIcons.bank(),
            contentDescription = null,
            modifier = if (currentRoute == "accounts") Modifier.size(36.dp)
            else Modifier.size(28.dp),
        )
    },
    BottomNavItem("taxes") {
        Icon(
            painter = AppIcons.wallet(),
            contentDescription = null,
            modifier = if (currentRoute == "taxes") Modifier.size(36.dp)
            else Modifier.size(28.dp),
        )
    },
    BottomNavItem("consultant") {
        Icon(
            painter = AppIcons.agent(),
            contentDescription = null,
            modifier = if (currentRoute == "consultant") Modifier.size(36.dp)
            else Modifier.size(28.dp),
        )
    },
)

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
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    item.icon()
                }
            }
        }
    }
}



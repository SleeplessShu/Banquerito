package com.sleeplessdog.banquerito.android.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sleeplessdog.banquerito.android.R


data class BottomNavItem(
    val route: String,
    val label: String,
    val iconRes: Int
)

val bottomNavItems = listOf(
    BottomNavItem("operations", "Операции", R.drawable.ic_payments),
    BottomNavItem("accounts", "Счета", R.drawable.ic_bank),
    BottomNavItem("taxes", "Налоги", R.drawable.ic_wallet),
    BottomNavItem("consultant", "Консультант", R.drawable.ic_agent),
)

@Composable
fun BottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
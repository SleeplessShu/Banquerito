package com.sleeplessdog.banquerito.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sleeplessdog.banquerito.ui.screens.PlaceholderScreen
import com.sleeplessdog.banquerito.ui.screens.accounts.AccountDetailScreen
import com.sleeplessdog.banquerito.ui.screens.accounts.AccountsScreen
import com.sleeplessdog.banquerito.ui.screens.planning.PlanningScreen
import com.sleeplessdog.banquerito.ui.screens.settings.SettingsScreen
import androidx.compose.runtime.getValue

sealed class Screen(val route: String) {
    data object Accounts : Screen("accounts")
    data object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: String) = "account_detail/$accountId"
    }
    data object Operations : Screen("operations")
    data object Taxes : Screen("taxes")
    data object Consultant : Screen("consultant")
    data object Settings : Screen("settings")
}

val bottomNavRoutes = listOf("accounts", "operations", "taxes", "consultant")

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Settings.route &&
            currentRoute != Screen.AccountDetail.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) BottomNav(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Accounts.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Accounts.route) {
                AccountsScreen(
                    onAccountClick = { accountId ->
                        navController.navigate(Screen.AccountDetail.createRoute(accountId))
                    }
                )
            }
            composable(Screen.AccountDetail.route) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
                AccountDetailScreen(
                    accountId = accountId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Operations.route) {
                PlanningScreen(
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(Screen.Taxes.route) {
                PlaceholderScreen("Налоги")
            }
            composable(Screen.Consultant.route) {
                PlaceholderScreen("Консультант")
            }
            composable(Screen.Settings.route) {
                PlaceholderScreen("Настройки")
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
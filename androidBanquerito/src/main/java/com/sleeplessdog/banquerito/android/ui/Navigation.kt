package com.sleeplessdog.banquerito.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sleeplessdog.banquerito.ui.screens.PlaceholderScreen
import com.sleeplessdog.banquerito.ui.screens.accounts.AccountDetailScreen
import com.sleeplessdog.banquerito.ui.screens.accounts.AccountsScreen

sealed class Screen(val route: String) {
    data object Accounts : Screen("accounts")
    data object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: String) = "account_detail/$accountId"
    }
    data object Operations : Screen("operations")
    data object Taxes : Screen("taxes")
    data object Consultant : Screen("consultant")
}

val bottomNavRoutes = listOf("accounts", "operations", "taxes", "consultant")

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNav(navController) }
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
                PlaceholderScreen("Операции")
            }
            composable(Screen.Taxes.route) {
                PlaceholderScreen("Налоги")
            }
            composable(Screen.Consultant.route) {
                PlaceholderScreen("Консультант")
            }
        }
    }
}
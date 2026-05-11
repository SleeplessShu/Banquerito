package com.sleeplessdog.banquerito.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sleeplessdog.banquerito.ui.screens.accounts.AccountDetailScreen
import com.sleeplessdog.banquerito.ui.screens.accounts.AccountsScreen

sealed class Screen(val route: String) {
    data object Accounts : Screen("accounts")
    data object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: String) = "account_detail/$accountId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Accounts.route
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
    }
}
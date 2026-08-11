package com.sleeplessdog.banquerito.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sleeplessdog.banquerito.ui.screens.accounts.AccountDetailScreen
import com.sleeplessdog.banquerito.ui.screens.accounts.AccountsScreen
import com.sleeplessdog.banquerito.ui.screens.planning.PlanningScreen
import com.sleeplessdog.banquerito.ui.screens.settings.SettingsScreen
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleeplessdog.banquerito.data.interfaces.ISettingsRepository
import com.sleeplessdog.banquerito.ui.screens.BottomNav
import com.sleeplessdog.banquerito.ui.screens.consultant.ConsultantScreen
import com.sleeplessdog.banquerito.ui.screens.first_launch.OnboardingScreen
import com.sleeplessdog.banquerito.ui.screens.tax.TaxesScreen
import org.koin.compose.koinInject

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")

    data object Accounts : Screen("accounts")
    data object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: String) = "account_detail/$accountId"
    }

    data object Operations : Screen("operations")
    data object Taxes : Screen("taxes")
    data object Consultant : Screen("consultant")
    data object Settings : Screen("settings")
}

@Composable
fun AppNavigation( settingsRepository: ISettingsRepository = koinInject()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar =
        currentRoute != Screen.AccountDetail.route && currentRoute != Screen.Onboarding.route

    val isFirstLaunch by settingsRepository.isFirstLaunch()
        .collectAsStateWithLifecycle(initialValue = null)
    if (isFirstLaunch == null) return

    Scaffold(
        bottomBar = {
            if (showBottomBar) BottomNav(navController)
        }) { padding ->
        val startDestination = if (isFirstLaunch == true) {
            Screen.Onboarding.route
        } else {
            Screen.Accounts.route
        }
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Accounts.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    })
            }
            composable(Screen.Accounts.route) {
                AccountsScreen(
                    onAccountClick = { accountId ->
                        navController.navigate(Screen.AccountDetail.createRoute(accountId))
                    })
            }
            composable(Screen.AccountDetail.route) { backStackEntry ->
                val accountId =
                    backStackEntry.arguments?.getString("accountId") ?: return@composable
                AccountDetailScreen(
                    accountId = accountId, onBack = { navController.popBackStack() })
            }
            composable(Screen.Operations.route) {
                PlanningScreen()
            }
            composable(Screen.Taxes.route) {
                TaxesScreen()
            }
            composable(Screen.Consultant.route) {
                ConsultantScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
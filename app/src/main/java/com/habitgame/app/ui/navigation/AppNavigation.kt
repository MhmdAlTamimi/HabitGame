package com.habitgame.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habitgame.app.ui.screens.history.HistoryDetailScreen
import com.habitgame.app.ui.screens.history.HistoryScreen
import com.habitgame.app.ui.screens.home.HomeScreen
import com.habitgame.app.ui.screens.settings.SettingsScreen
import com.habitgame.app.ui.screens.setup.SetupScreen
import com.habitgame.app.ui.viewmodels.MainViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
) {
    val hasActiveChallenge by viewModel.hasActiveChallenge.collectAsState()

    if (hasActiveChallenge == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (hasActiveChallenge == true) NavRoutes.HOME else NavRoutes.SETUP

    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.SETUP) {
            SetupScreen(
                viewModel = viewModel,
                onChallengeCreated = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SETUP) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToHistory = { navController.navigate(NavRoutes.HISTORY) },
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) }
            )
        }
        composable(NavRoutes.HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogClick = { logId ->
                    navController.navigate(NavRoutes.historyDetail(logId))
                }
            )
        }
        composable(
            NavRoutes.HISTORY_DETAIL,
            arguments = listOf(navArgument("logId") { type = NavType.IntType })
        ) { backStackEntry ->
            val logId = backStackEntry.arguments?.getInt("logId") ?: return@composable
            HistoryDetailScreen(
                viewModel = viewModel,
                logId = logId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onChallengeEnded = {
                    navController.navigate(NavRoutes.SETUP) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

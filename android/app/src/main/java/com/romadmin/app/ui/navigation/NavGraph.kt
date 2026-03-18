package com.romadmin.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.romadmin.app.data.preferences.AppPreferences
import com.romadmin.app.ui.downloads.DownloadManagerScreen
import com.romadmin.app.ui.games.GameDetailScreen
import com.romadmin.app.ui.games.GameListScreen
import com.romadmin.app.ui.home.HomeScreen
import com.romadmin.app.ui.platforms.PlatformListScreen
import com.romadmin.app.ui.settings.SettingsScreen
import com.romadmin.app.ui.setup.SetupScreen
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class Screen(val route: String) {
    data object Setup : Screen("setup")
    data object Home : Screen("home")
    data object Platforms : Screen("platforms")
    data object Games : Screen("platforms/{platformId}") {
        fun createRoute(platformId: Int) = "platforms/$platformId"
    }
    data object GameDetail : Screen("games/{gameId}") {
        fun createRoute(gameId: Int) = "games/$gameId"
    }
    data object Downloads : Screen("downloads")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home),
    BottomNavItem(Screen.Platforms, "Library", Icons.Filled.SportsEsports),
    BottomNavItem(Screen.Downloads, "Downloads", Icons.Filled.Download),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomAdminNavHost() {
    val navController = rememberNavController()
    val prefs: AppPreferences = hiltViewModel<NavViewModel>().prefs
    val isSetup by prefs.isSetupComplete.collectAsState(initial = null)

    if (isSetup == null) return // loading

    val startDestination = if (isSetup == true) Screen.Home.route else Screen.Setup.route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Platforms.route,
        Screen.Downloads.route,
        Screen.Settings.route,
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(Screen.Setup.route) {
                SetupScreen(
                    onSetupComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Setup.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen()
            }

            composable(Screen.Platforms.route) {
                PlatformListScreen(
                    onPlatformClick = { platformId ->
                        navController.navigate(Screen.Games.createRoute(platformId))
                    }
                )
            }

            composable(
                route = Screen.Games.route,
                arguments = listOf(navArgument("platformId") { type = NavType.IntType }),
            ) {
                GameListScreen(
                    onGameClick = { gameId ->
                        navController.navigate(Screen.GameDetail.createRoute(gameId))
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.GameDetail.route,
                arguments = listOf(navArgument("gameId") { type = NavType.IntType }),
            ) {
                GameDetailScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Downloads.route) {
                DownloadManagerScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

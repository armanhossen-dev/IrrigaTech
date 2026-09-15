package com.ahrn.irrigatech.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ahrn.irrigatech.data.model.MotorId
import com.ahrn.irrigatech.feedback.FeedbackScreen
import com.ahrn.irrigatech.ui.screens.about.AboutScreen
import com.ahrn.irrigatech.ui.screens.alerts.AlertsScreen
import com.ahrn.irrigatech.ui.screens.dashboard.DashboardScreen
import com.ahrn.irrigatech.ui.screens.login.LoginScreen
import com.ahrn.irrigatech.ui.screens.motor.MotorDetailScreen
import com.ahrn.irrigatech.ui.screens.settings.SettingsScreen
import com.ahrn.irrigatech.ui.screens.setup.SetupScreen
import com.ahrn.irrigatech.ui.screens.splash.SplashScreen

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomTabs = listOf(
    BottomTab(Routes.DASHBOARD, "Dashboard", Icons.Outlined.Dashboard),
    BottomTab(Routes.ALERTS, "Alerts", Icons.Outlined.Notifications),
    BottomTab(Routes.SETTINGS, "Settings", Icons.Outlined.Settings),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = { signedIn, hasDevice ->
                    val target = when {
                        !signedIn -> Routes.LOGIN
                        hasDevice -> Routes.HOME
                        else -> Routes.SETUP
                    }
                    navController.navigate(target) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onAuthenticated = { hasDevice ->
                    val target = if (hasDevice) Routes.HOME else Routes.SETUP
                    navController.navigate(target) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.SETUP) {
            SetupScreen(
                onDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeShell(
                onSignedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeShell(onSignedOut: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()


    val showBar = bottomTabs.any { tab ->
        backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        val selected = backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(
                bottom = if (showBar) innerPadding.calculateBottomPadding() else 0.dp,
            ).statusBarsPadding(),
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onOpenNotifications = {
                        navController.navigate(Routes.ALERTS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Routes.ALERTS) {
                AlertsScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onOpenAbout = { navController.navigate(Routes.ABOUT) },
                    onOpenFeedback = { navController.navigate(Routes.FEEDBACK) },
                    onOpenSetup = { navController.navigate(Routes.SETUP) },
                    onSignedOut = onSignedOut,
                )
            }
            composable(Routes.SETUP) {
                SetupScreen(onDone = { navController.popBackStack() })
            }
            composable(
                route = Routes.MOTOR,
                arguments = listOf(navArgument("motorId") { type = NavType.StringType }),
            ) { entry ->
                val raw = entry.arguments?.getString("motorId")
                val motor = MotorId.entries.firstOrNull { it.name == raw } ?: MotorId.FIELD
                MotorDetailScreen(motor = motor, onBack = { navController.popBackStack() })
            }
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.FEEDBACK) {
                FeedbackScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

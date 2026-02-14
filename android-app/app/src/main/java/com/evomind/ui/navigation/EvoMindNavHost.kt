package com.evomind.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.evomind.ui.screens.agent.AgentScreen
import com.evomind.ui.screens.challenges.ChallengesScreen
import com.evomind.ui.screens.corpus.CorpusScreen
import com.evomind.ui.screens.feed.FeedScreen
import com.evomind.ui.screens.home.HomeScreen
import com.evomind.ui.screens.login.LoginScreen
import com.evomind.ui.screens.ocr.OcrImportScreen
import com.evomind.ui.screens.ocr.OcrResultScreen
import com.evomind.ui.screens.profile.ProfileScreen
import com.evomind.ui.screens.sources.SourcesScreen
import com.evomind.ui.screens.welcome.WelcomeScreen

@Composable
fun EvoMindNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Welcome.route
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth Flow
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                // TODO: RegisterScreen - 使用 LoginScreen 临时占位
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            // Main Flow
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAgent = { navController.navigate(Screen.Agent.route) },
                    onNavigateToAdd = { navController.navigate(Screen.ScreenshotImport.route) }
                )
            }

            composable(Screen.Feed.route) {
                FeedScreen()
            }

            composable(Screen.Sources.route) {
                SourcesScreen()
            }

            composable(Screen.Corpus.route) {
                CorpusScreen()
            }

            composable(Screen.Agent.route) {
                AgentScreen()
            }

            composable(Screen.Challenges.route) {
                ChallengesScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            // Feature Screens
            composable(Screen.ScreenshotImport.route) {
                OcrImportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToResult = { taskId ->
                        navController.navigate(Screen.OcrResult.createRoute(taskId))
                    }
                )
            }

            composable(
                route = Screen.OcrResult.route,
                arguments = listOf(
                    navArgument("taskId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                OcrResultScreen(
                    taskId = taskId,
                    onNavigateBack = { navController.popBackStack() },
                    onImportComplete = {
                        navController.navigate(Screen.Sources.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(Screen.VoiceRecord.route) {
                // TODO: VoiceRecordScreen
            }

            composable(Screen.EvolutionPlan.route) {
                // TODO: EvolutionPlanScreen
            }

            composable(Screen.AbilityReport.route) {
                // TODO: AbilityReportScreen
            }

            composable(Screen.Subscription.route) {
                // TODO: SubscriptionScreen
            }

            composable(Screen.Settings.route) {
                // TODO: SettingsScreen
            }
        }
    }
}

private fun shouldShowBottomBar(currentRoute: String?): Boolean {
    return when (currentRoute) {
        Screen.Home.route,
        Screen.Feed.route,
        Screen.Sources.route,
        Screen.Corpus.route,
        Screen.Agent.route,
        Screen.Challenges.route,
        Screen.Profile.route -> true
        else -> false
    }
}

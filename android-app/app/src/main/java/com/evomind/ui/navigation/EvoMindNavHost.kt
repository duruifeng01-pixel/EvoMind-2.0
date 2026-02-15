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
import com.evomind.ui.components.linkimport.LinkImportScreen
import com.evomind.ui.screens.agent.AgentScreen
import com.evomind.ui.screens.card.CognitiveCardScreen
import com.evomind.ui.screens.challenges.ChallengesScreen
import com.evomind.ui.screens.corpus.CorpusScreen
import com.evomind.ui.screens.feed.FeedScreen
import com.evomind.ui.screens.home.HomeScreen
import com.evomind.ui.screens.share.ShareImageScreen
import com.evomind.ui.screens.login.LoginScreen
import com.evomind.ui.screens.login.ForgotPasswordScreen
import com.evomind.ui.screens.login.ResetPasswordScreen
import com.evomind.ui.screens.mindmap.MindMapScreen
import com.evomind.ui.screens.ocr.OcrImportScreen
import com.evomind.ui.screens.ocr.OcrResultScreen
import com.evomind.ui.screens.privacy.*
import com.evomind.ui.screens.profile.ComputingCostScreen
import com.evomind.ui.screens.profile.ProfileScreen
import com.evomind.ui.screens.settings.SettingsScreen
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
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToResetPassword = { phone ->
                        navController.navigate(Screen.ResetPassword.createRoute(phone))
                    }
                )
            }

            composable(
                route = Screen.ResetPassword.route,
                arguments = listOf(
                    navArgument("phone") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val phone = backStackEntry.arguments?.getString("phone") ?: ""
                ResetPasswordScreen(
                    phone = phone,
                    onNavigateBack = { navController.popBackStack() },
                    onResetSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    }
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
                ProfileScreen(
                    onNavigateToComputingCost = { navController.navigate(Screen.ComputingCost.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.ComputingCost.route) {
                ComputingCostScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Settings & Privacy Screens
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                    onNavigateToUserAgreement = { navController.navigate(Screen.UserAgreement.route) },
                    onNavigateToAigcCompliance = { navController.navigate(Screen.AigcCompliance.route) },
                    onNavigateToDataExport = { navController.navigate(Screen.DataExport.route) },
                    onNavigateToAccountDeletion = { navController.navigate(Screen.AccountDeletion.route) },
                    onLogout = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.UserAgreement.route) {
                UserAgreementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AigcCompliance.route) {
                AigcComplianceScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DataExport.route) {
                DataExportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AccountDeletion.route) {
                AccountDeletionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
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

            composable(Screen.LinkImport.route) {
                LinkImportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.VoiceRecord.route) {
                // TODO: VoiceRecordScreen
            }

            composable(Screen.EvolutionPlan.route) {
                // TODO: EvolutionPlanScreen
            }

            composable(Screen.GrowthStats.route) {
                GrowthStatsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AbilityReport.route) {
                AbilityReportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ShareImage.route) {
                ShareImageScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Subscription.route) {
                // TODO: SubscriptionScreen
            }

            composable(
                route = Screen.MindMap.route,
                arguments = listOf(navArgument("topicId") { type = NavType.StringType })
            ) { backStackEntry ->
                val topicId = backStackEntry.arguments?.getString("topicId") ?: "0"
                val cardId = topicId.toLongOrNull() ?: 0L
                MindMapScreen(
                    cardId = cardId,
                    cardTitle = "知识脑图",
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CognitiveCard.route,
                arguments = listOf(navArgument("cardId") { type = NavType.StringType })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: "0"
                CognitiveCardScreen(
                    cardId = cardId,
                    onBack = { navController.popBackStack() },
                    onNavigateToMindMap = { id ->
                        navController.navigate(Screen.MindMap.createRoute(id))
                    },
                    onNavigateToSource = { url ->
                        // TODO: 打开外部浏览器
                    },
                    onStartDiscussion = { id ->
                        // TODO: 跳转苏格拉底对话
                    },
                    onShare = {
                        // TODO: 分享功能
                    }
                )
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

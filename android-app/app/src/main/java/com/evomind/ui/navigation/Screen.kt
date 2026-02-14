package com.evomind.ui.navigation

sealed class Screen(val route: String) {
    // Auth Screens
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Verification : Screen("verification")

    // Main Screens
    object Home : Screen("home")
    object Feed : Screen("feed")
    object Sources : Screen("sources")
    object Corpus : Screen("corpus")
    object Agent : Screen("agent")
    object Challenges : Screen("challenges")
    object Profile : Screen("profile")

    // Feature Screens
    object ScreenshotImport : Screen("screenshot_import")
    object VoiceRecord : Screen("voice_record")
    object CognitiveCard : Screen("cognitive_card/{cardId}") {
        fun createRoute(cardId: String) = "cognitive_card/$cardId"
    }
    object MindMap : Screen("mind_map/{topicId}") {
        fun createRoute(topicId: String) = "mind_map/$topicId"
    }
    object AgentChat : Screen("agent_chat")
    object EvolutionPlan : Screen("evolution_plan")
    object AbilityReport : Screen("ability_report")
    object ShareImage : Screen("share_image")
    object Subscription : Screen("subscription")
    object Settings : Screen("settings")
    object Help : Screen("help")

    // Nested Navigation
    object Main : Screen("main")
}

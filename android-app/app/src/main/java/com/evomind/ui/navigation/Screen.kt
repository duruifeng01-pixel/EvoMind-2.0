package com.evomind.ui.navigation

sealed class Screen(val route: String) {
    // Auth Screens
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Verification : Screen("verification")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{phone}") {
        fun createRoute(phone: String) = "reset_password/$phone"
    }

    // Main Screens
    object Home : Screen("home")
    object Feed : Screen("feed")
    object Sources : Screen("sources")
    object Corpus : Screen("corpus")
    object Agent : Screen("agent")
    object Challenges : Screen("challenges")
    object Profile : Screen("profile")

    // Study Group Screens (feat_017)
    object StudyGroups : Screen("study_groups")
    object StudyGroupDetail : Screen("study_group/{groupId}") {
        fun createRoute(groupId: String) = "study_group/$groupId"
    }
    object GroupDiscussion : Screen("group_discussion/{discussionId}") {
        fun createRoute(discussionId: String) = "group_discussion/$discussionId"
    }

    // Feature Screens
    object ScreenshotImport : Screen("screenshot_import")
    object OcrResult : Screen("ocr_result/{taskId}") {
        fun createRoute(taskId: String) = "ocr_result/$taskId"
    }
    object LinkImport : Screen("link_import")
    object VoiceRecord : Screen("voice_record")
    object VoiceNoteList : Screen("voice_note_list")
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
    object ComputingCost : Screen("computing_cost")
    object Settings : Screen("settings")
    object Help : Screen("help")

    // Privacy Screens
    object PrivacyPolicy : Screen("privacy_policy")
    object UserAgreement : Screen("user_agreement")
    object AigcCompliance : Screen("aigc_compliance")
    object DataExport : Screen("data_export")
    object AccountDeletion : Screen("account_deletion")
    object FirstLaunch : Screen("first_launch")

    // Nested Navigation
    object Main : Screen("main")
}

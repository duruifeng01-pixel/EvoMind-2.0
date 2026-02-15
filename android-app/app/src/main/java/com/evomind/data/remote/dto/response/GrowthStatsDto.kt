package com.evomind.data.remote.dto.response

data class GrowthStatsDto(
    val totalLearningHours: Double?,
    val totalCardsRead: Int?,
    val totalMindMapNodes: Int?,
    val totalNotesCreated: Int?,
    val agentEvolutionLevel: Int?,
    val weeklyStats: WeeklyStatsDto?,
    val monthlyStats: MonthlyStatsDto?,
    val streakDays: Int?,
    val lastActiveDate: String?
)

data class WeeklyStatsDto(
    val weekStartDate: String?,
    val days: List<DailyStatsDto>?
)

data class MonthlyStatsDto(
    val month: String?,
    val totalHours: Double?,
    val totalCards: Int?
)

data class DailyStatsDto(
    val date: String?,
    val learningMinutes: Int?,
    val cardsRead: Int?,
    val notesCreated: Int?
)

data class AbilityProfileDto(
    val userId: Long?,
    val overallLevel: Int?,
    val knowledgeBreadth: Int?,
    val knowledgeDepth: Int?,
    val criticalThinking: Int?,
    val communication: Int?,
    val creativity: Int?,
    val lastUpdated: String?
)

data class EvolutionProgressDto(
    val currentStage: String?,
    val progress: Int?,
    val nextMilestone: String?,
    val milestones: List<MilestoneDto>?
)

data class MilestoneDto(
    val name: String?,
    val description: String?,
    val isCompleted: Boolean?,
    val completedAt: String?
)

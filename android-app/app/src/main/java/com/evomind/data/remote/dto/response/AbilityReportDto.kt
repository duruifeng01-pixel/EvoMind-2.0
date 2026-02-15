package com.evomind.data.remote.dto.response

data class AbilityReportDto(
    val id: Long?,
    val userId: Long?,
    val reportType: String?,
    val title: String?,
    val period: String?,
    val summary: String?,
    val knowledgeBreadth: Int?,
    val knowledgeDepth: Int?,
    val criticalThinking: Int?,
    val learningEfficiency: Int?,
    val totalCardsRead: Int?,
    val totalLearningHours: Double?,
    val streakDays: Int?,
    val topTopics: List<String>?,
    val stageSuggestions: List<String>?,
    val createdAt: String?
)

data class KnowledgeGraphDto(
    val nodes: List<KnowledgeNodeDto>?,
    val edges: List<KnowledgeEdgeDto>?
)

data class KnowledgeNodeDto(
    val id: String?,
    val name: String?,
    val category: String?,
    val importance: Int?
)

data class KnowledgeEdgeDto(
    val source: String?,
    val target: String?,
    val relation: String?
)

data class GrowthCurvePointDto(
    val date: String?,
    val value: Double?,
    val label: String?
)

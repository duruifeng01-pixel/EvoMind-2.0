package com.evomind.data.remote.dto.response

data class FeedItemDto(
    val id: Long?,
    val type: String?,
    val cardId: Long?,
    val cardTitle: String?,
    val cardSummary: String?,
    val sourceTitle: String?,
    val sourceFavicon: String?,
    val hasConflict: Boolean?,
    val conflictCount: Int?,
    val createdAt: String?
)

data class FeedResponseDto(
    val items: List<FeedItemDto>?,
    val page: Int?,
    val size: Int?,
    val totalElements: Long?,
    val totalPages: Int?
)

data class FeedStatsDto(
    val totalCards: Int?,
    val todayCards: Int?,
    val unreadCards: Int?,
    val hasConflictCards: Int?
)

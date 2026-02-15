package com.evomind.data.remote.dto.response

data class CardResponseDto(
    val id: Long?,
    val userId: Long?,
    val title: String?,
    val summaryText: String?,
    val oneSentenceSummary: String?,
    val sourceId: Long?,
    val sourceUrl: String?,
    val sourceTitle: String?,
    val originalContentId: Long?,
    val mindmapJson: String?,
    val isFavorite: Boolean?,
    val isArchived: Boolean?,
    val hasConflict: Boolean?,
    val conflictCardIds: String?,
    val viewCount: Int?,
    val lastViewedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)

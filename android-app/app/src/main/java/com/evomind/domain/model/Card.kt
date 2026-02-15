package com.evomind.domain.model

data class Card(
    val id: Long = 0,
    val userId: Long = 0,
    val title: String = "",
    val summaryText: String? = null,
    val oneSentenceSummary: String? = null,
    val sourceId: Long? = null,
    val sourceUrl: String? = null,
    val sourceTitle: String? = null,
    val originalContentId: Long? = null,
    val mindmapJson: String? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val hasConflict: Boolean = false,
    val conflictCardIds: String? = null,
    val viewCount: Int = 0,
    val lastViewedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

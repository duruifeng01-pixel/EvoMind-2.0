package com.evomind.data.remote.dto.response

data class SourceDto(
    val id: Long?,
    val userId: Long?,
    val name: String?,
    val url: String?,
    val type: String?,
    val favicon: String?,
    val isActive: Boolean?,
    val lastFetchedAt: String?,
    val articleCount: Int?,
    val createdAt: String?
)

data class SourceStatsDto(
    val sourceId: Long?,
    val totalArticles: Int?,
    val unreadArticles: Int?,
    val last24hArticles: Int?
)

package com.evomind.data.remote.dto.response

import com.evomind.data.remote.dto.ApiResponseDto

/**
 * 用户语料库响应DTO
 */
data class UserCorpusResponseDto(
    val id: Long,
    val userId: Long,
    val title: String,
    val summaryText: String?,
    val oneSentenceSummary: String?,
    val corpusType: String,
    val corpusTypeDisplay: String,
    val sourceType: String?,
    val sourceId: Long?,
    val sourceRef: String?,
    val discussionId: Long?,
    val keywords: String?,
    val readingTimeMinutes: Int?,
    val isFavorite: Boolean,
    val isPinned: Boolean,
    val pinnedAt: String?,
    val isArchived: Boolean,
    val archivedAt: String?,
    val viewCount: Int,
    val lastViewedAt: String?,
    val relatedCardId: Long?,
    val createdAt: String,
    val updatedAt: String
)

/**
 * 语料库详情响应DTO
 */
data class UserCorpusDetailResponseDto(
    val id: Long,
    val userId: Long,
    val title: String,
    val contentText: String?,
    val summaryText: String?,
    val oneSentenceSummary: String?,
    val corpusType: String,
    val corpusTypeDisplay: String,
    val sourceType: String?,
    val sourceId: Long?,
    val sourceRef: String?,
    val discussionId: Long?,
    val keywords: String?,
    val readingTimeMinutes: Int?,
    val isFavorite: Boolean,
    val isPinned: Boolean,
    val pinnedAt: String?,
    val isArchived: Boolean,
    val archivedAt: String?,
    val viewCount: Int,
    val lastViewedAt: String?,
    val relatedCardId: Long?,
    val createdAt: String,
    val updatedAt: String
)

/**
 * 语料库统计响应DTO
 */
data class CorpusStatsResponseDto(
    val total: Long,
    val socraticInsightCount: Long,
    val userNoteCount: Long,
    val highlightCount: Long,
    val aiSummaryCount: Long
)

/**
 * 分页响应包装类
 */
data class PagedCorpusResponseDto(
    val content: List<UserCorpusResponseDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
    val first: Boolean
)

typealias ApiResponse<T> = ApiResponseDto<T>

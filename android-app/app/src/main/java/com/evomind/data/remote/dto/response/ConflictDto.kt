package com.evomind.data.remote.dto.response

data class ConflictDto(
    val id: Long?,
    val cardId: Long?,
    val conflictType: String?,
    val title: String?,
    val summary: String?,
    val relatedCards: List<RelatedCardDto>?,
    val createdAt: String?
)

data class RelatedCardDto(
    val id: Long?,
    val title: String?,
    val summary: String?,
    val sourceTitle: String?,
    val stance: String?
)

data class ConflictComparisonDto(
    val conflictId: Long?,
    val card1: CardSummaryDto?,
    val card2: CardSummaryDto?,
    val differences: List<ConflictPointDto>?
)

data class CardSummaryDto(
    val id: Long?,
    val title: String?,
    val summary: String?,
    val sourceTitle: String?,
    val keyPoints: List<String>?
)

data class ConflictPointDto(
    val topic: String?,
    val viewpoint1: String?,
    val viewpoint2: String?
)

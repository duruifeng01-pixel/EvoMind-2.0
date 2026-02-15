package com.evomind.data.remote.dto.response

data class MindMapResponseDto(
    val cardId: Long?,
    val cardTitle: String?,
    val nodes: List<MindMapNodeDto>?,
    val totalNodes: Int?
)

data class MindMapNodeDto(
    val nodeId: String?,
    val parentNodeId: String?,
    val text: String?,
    val description: String?,
    val nodeType: String?,
    val level: Int?,
    val sortOrder: Int?,
    val hasOriginalReference: Boolean?,
    val originalContentId: Long?,
    val originalParagraphIndex: Int?,
    val isExpanded: Boolean?,
    val children: List<MindMapNodeDto>?
)

data class DrilldownResponseDto(
    val nodeId: String?,
    val nodeText: String?,
    val originalContent: String?,
    val paragraphs: List<ParagraphDto>?,
    val relatedCards: List<RelatedCardDto>?
)

data class ParagraphDto(
    val index: Int?,
    val content: String?,
    val highlightedText: String?
)

data class RelatedCardDto(
    val id: Long?,
    val title: String?,
    val summary: String?
)

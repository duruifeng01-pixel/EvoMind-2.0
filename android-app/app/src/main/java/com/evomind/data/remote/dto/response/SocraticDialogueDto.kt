package com.evomind.data.remote.dto.response

data class SocraticDialogueDto(
    val id: Long?,
    val userId: Long?,
    val cardId: Long?,
    val title: String?,
    val status: String?,
    val questionCount: Int?,
    val insightCount: Int?,
    val createdAt: String?,
    val updatedAt: String?
)

data class QuestionDto(
    val id: Long?,
    val dialogueId: Long?,
    val questionText: String?,
    val answerText: String?,
    val questionType: String?,
    val orderIndex: Int?,
    val createdAt: String?
)

data class SocraticInsightDto(
    val id: Long?,
    val dialogueId: Long?,
    val title: String?,
    val content: String?,
    val relatedConcepts: List<String>?,
    val createdAt: String?
)

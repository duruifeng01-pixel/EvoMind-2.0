package com.evomind.data.remote.dto

/**
 * 创建语料请求DTO
 */
data class CreateCorpusRequestDto(
    val title: String,
    val contentText: String? = null,
    val summaryText: String? = null,
    val oneSentenceSummary: String? = null,
    val corpusType: String = "USER_NOTE",
    val sourceType: String? = null,
    val sourceId: Long? = null,
    val sourceRef: String? = null,
    val discussionId: Long? = null,
    val keywords: String? = null,
    val readingTimeMinutes: Int? = null,
    val relatedCardId: Long? = null
)

package com.evomind.domain.model

import java.time.LocalDateTime

/**
 * 用户语料库数据模型
 */
data class UserCorpus(
    val id: Long = 0,
    val userId: Long = 0,
    val title: String = "",
    val contentText: String? = null,
    val summaryText: String? = null,
    val oneSentenceSummary: String? = null,
    val corpusType: CorpusType = CorpusType.SOCRATIC_INSIGHT,
    val sourceType: SourceType? = null,
    val sourceId: Long? = null,
    val sourceRef: String? = null,
    val discussionId: Long? = null,
    val keywords: String? = null,
    val readingTimeMinutes: Int? = null,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val pinnedAt: LocalDateTime? = null,
    val isArchived: Boolean = false,
    val archivedAt: LocalDateTime? = null,
    val viewCount: Int = 0,
    val lastViewedAt: LocalDateTime? = null,
    val relatedCardId: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    /**
     * 语料类型枚举
     */
    enum class CorpusType(val displayName: String, val description: String) {
        SOCRATIC_INSIGHT("苏格拉底洞察", "通过苏格拉底式对话生成的深度思考洞察"),
        USER_NOTE("用户笔记", "用户自己记录的笔记和随想"),
        HIGHLIGHT("收藏高亮", "从外部内容中提取的高亮和标注"),
        AI_SUMMARY("AI总结", "AI辅助生成的内容总结"),
        REFLECTION("反思记录", "用户对某主题的深度反思"),
        INSIGHT("认知洞察", "其他类型的认知洞察");

        companion object {
            fun fromString(value: String): CorpusType {
                return entries.find { it.name == value } ?: INSIGHT
            }
        }
    }

    /**
     * 来源类型枚举
     */
    enum class SourceType {
        SOCRATIC_DIALOGUE,
        DISCUSSION,
        CARD,
        MANUAL_INPUT,
        VOICE_NOTE,
        OCR_IMPORT,
        LINK_SCRAPE;

        companion object {
            fun fromString(value: String?): SourceType? {
                return value?.let { entries.find { e -> e.name == it } }
            }
        }
    }

    /**
     * 获取类型显示名称
     */
    fun getTypeDisplayName(): String = corpusType.displayName

    /**
     * 获取关键词列表
     */
    fun getKeywordsList(): List<String> {
        return keywords?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    /**
     * 获取内容摘要
     */
    fun getDisplaySummary(): String {
        return when {
            !oneSentenceSummary.isNullOrBlank() -> oneSentenceSummary
            !summaryText.isNullOrBlank() -> summaryText
            !contentText.isNullOrBlank() -> contentText.take(100)
            else -> ""
        }
    }
}

/**
 * 语料库统计信息
 */
data class CorpusStats(
    val total: Long = 0,
    val socraticInsightCount: Long = 0,
    val userNoteCount: Long = 0,
    val highlightCount: Long = 0,
    val aiSummaryCount: Long = 0
) {
    /**
     * 获取总计
     */
    fun getTotal(): Long = total

    /**
     * 获取洞察数量
     */
    fun getInsightCount(): Long = socraticInsightCount

    /**
     * 获取笔记数量
     */
    fun getNoteCount(): Long = userNoteCount
}

/**
 * 分页结果
 */
data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasMore: Boolean
)

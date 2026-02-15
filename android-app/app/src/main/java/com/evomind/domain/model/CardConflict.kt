package com.evomind.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 卡片冲突数据模型
 */
data class CardConflict(
    val id: Long,
    val cardId1: Long,
    val cardId2: Long,
    val cardTitle1: String,
    val cardTitle2: String,
    val cardViewpoint1: String,
    val cardViewpoint2: String,
    val conflictType: ConflictType,
    val conflictDescription: String,
    val topic: String,
    val similarityScore: BigDecimal,
    val conflictScore: BigDecimal,
    val isAcknowledged: Boolean,
    val aiAnalysis: String,
    val createdAt: LocalDateTime,
    val acknowledgedAt: LocalDateTime? = null
) {
    /**
     * 获取冲突类型描述
     */
    fun getConflictTypeDescription(): String {
        return conflictType.description
    }

    /**
     * 获取冲突严重程度（基于conflictScore）
     */
    fun getSeverity(): ConflictSeverity {
        return when {
            conflictScore.toDouble() >= 0.8 -> ConflictSeverity.HIGH
            conflictScore.toDouble() >= 0.6 -> ConflictSeverity.MEDIUM
            else -> ConflictSeverity.LOW
        }
    }
}

/**
 * 冲突类型枚举
 */
enum class ConflictType(val value: String, val description: String) {
    CONTRADICTORY("CONTRADICTORY", "观点对立"),
    COMPLEMENTARY("COMPLEMENTARY", "观点互补"),
    CHALLENGING("CHALLENGING", "挑战信念"),
    DIFFERENT_PERSPECTIVE("DIFFERENT_PERSPECTIVE", "不同视角"),
    TOPIC_OVERLAP("TOPIC_OVERLAP", "主题重叠"),
    UNKNOWN("UNKNOWN", "未知类型");

    companion object {
        fun fromValue(value: String?): ConflictType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}

/**
 * 冲突严重程度
 */
enum class ConflictSeverity(val level: Int, val description: String) {
    HIGH(3, "高"),
    MEDIUM(2, "中"),
    LOW(1, "低")
}

/**
 * 冲突列表结果
 */
data class ConflictListResult(
    val conflicts: List<CardConflict>,
    val totalCount: Int,
    val unresolvedCount: Int
)

/**
 * 冲突检测请求
 */
data class ConflictDetectRequest(
    val cardId: Long
)

/**
 * 冲突确认请求
 */
data class ConflictAcknowledgeRequest(
    val conflictId: Long
)

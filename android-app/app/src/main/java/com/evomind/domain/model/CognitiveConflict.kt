package com.evomind.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 认知冲突数据模型（新卡片与用户认知体系的冲突）
 */
data class CognitiveConflict(
    val id: Long,
    val cardId: Long,
    val cardTitle: String,
    val cardViewpoint: String,
    val profileId: Long,
    val topic: String,
    val userBelief: String,
    val conflictType: ConflictType,
    val conflictDescription: String,
    val conflictScore: BigDecimal,
    val isAcknowledged: Boolean,
    val isDismissed: Boolean,
    val aiAnalysis: String,
    val createdAt: LocalDateTime
) {
    /**
     * 获取冲突严重程度
     */
    fun getSeverity(): ConflictSeverity {
        return when {
            conflictScore.toDouble() >= 0.8 -> ConflictSeverity.HIGH
            conflictScore.toDouble() >= 0.6 -> ConflictSeverity.MEDIUM
            else -> ConflictSeverity.LOW
        }
    }

    /**
     * 获取冲突类型描述
     */
    fun getConflictTypeDescription(): String {
        return conflictType.description
    }
}

/**
 * 用户认知画像
 */
data class UserCognitiveProfile(
    val id: Long,
    val topic: String,
    val coreBelief: String,
    val beliefType: BeliefType,
    val confidenceLevel: BigDecimal,
    val evidenceCount: Int
)

/**
 * 信念类型
 */
enum class BeliefType(val description: String) {
    STRONG_CONViction("强信念"),
    MODERATE_STANCE("中等立场"),
    EXPLORING("探索中"),
    TENTATIVE("暂定")
}

/**
 * 冲突严重程度
 */
enum class ConflictSeverity(val level: Int, val description: String) {
    HIGH(3, "高"),
    MEDIUM(2, "中"),
    LOW(1, "低")
}

package com.evomind.domain.model

import java.time.LocalDateTime

/**
 * 苏格拉底式对话洞察
 */
data class SocraticInsight(
    val id: Long,
    val dialogueId: Long,
    val discussionId: Long,
    val coreInsight: String,
    val thinkingEvolution: List<ThinkingEvolution>,
    val turningPoints: List<KeyTurningPoint>,
    val unresolvedQuestions: List<String>,
    val reflectionSuggestion: String,
    val relatedCards: List<RelatedCard>,
    val roundStats: RoundStats,
    val generatedAt: LocalDateTime
) {
    /**
     * 获取思考的演变阶段数
     */
    fun getEvolutionStageCount(): Int {
        return thinkingEvolution.size
    }

    /**
     * 是否有转折点
     */
    fun hasTurningPoints(): Boolean {
        return turningPoints.isNotEmpty()
    }

    /**
     * 是否有未解问题
     */
    fun hasUnresolvedQuestions(): Boolean {
        return unresolvedQuestions.isNotEmpty()
    }

    /**
     * 获取思考深度等级（低/中/高）
     */
    fun getDepthLevel(): String {
        return when {
            roundStats.thinkingDepthScore >= 8 -> "深度思考"
            roundStats.thinkingDepthScore >= 5 -> "中度思考"
            else -> "初步探索"
        }
    }

    /**
     * 获取深度颜色标识
     */
    fun getDepthColor(): String {
        return when {
            roundStats.thinkingDepthScore >= 8 -> "#4CAF50" // 绿色
            roundStats.thinkingDepthScore >= 5 -> "#FF9800" // 橙色
            else -> "#9E9E9E" // 灰色
        }
    }
}

/**
 * 思考演变
 */
data class ThinkingEvolution(
    val stage: Int,
    val description: String,
    val userThinking: String,
    val aiGuidance: String
)

/**
 * 关键转折点
 */
data class KeyTurningPoint(
    val round: Int,
    val description: String,
    val beforeAfter: String
)

/**
 * 相关认知卡片
 */
data class RelatedCard(
    val cardId: Long,
    val title: String,
    val relevanceReason: String
)

/**
 * 轮次统计
 */
data class RoundStats(
    val totalRounds: Int,
    val avgResponseLength: Int,
    val depthDistribution: List<Int>,
    val thinkingDepthScore: Int
) {
    /**
     * 获取平均回复长度描述
     */
    fun getAvgLengthDescription(): String {
        return when {
            avgResponseLength > 300 -> "详细阐述"
            avgResponseLength > 150 -> "适中"
            else -> "简洁"
        }
    }

    /**
     * 获取深度分布的最大深度
     */
    fun getMaxDepthReached(): Int {
        return depthDistribution.maxOrNull() ?: 1
    }
}

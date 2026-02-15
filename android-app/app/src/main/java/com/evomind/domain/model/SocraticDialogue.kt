package com.evomind.domain.model

import java.time.LocalDateTime

/**
 * 苏格拉底式对话会话
 */
data class SocraticDialogue(
    val id: Long,
    val discussionId: Long,
    val discussionTitle: String?,
    val status: DialogueStatus,
    val currentRound: Int,
    val maxRounds: Int,
    val initialQuestion: String?,
    val finalInsight: String?,
    val totalMessages: Int,
    val canContinue: Boolean,
    val remainingRounds: Int,
    val lastMessageAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val lastMessage: SocraticMessage?
) {
    companion object {
        const val DEFAULT_MAX_ROUNDS = 5
        const val MAX_MESSAGE_LENGTH = 2000
    }

    /**
     * 获取进度百分比
     */
    fun getProgressPercent(): Int {
        return if (maxRounds > 0) {
            (currentRound.toFloat() / maxRounds * 100).toInt()
        } else {
            0
        }
    }

    /**
     * 是否已完成
     */
    fun isCompleted(): Boolean {
        return status == DialogueStatus.COMPLETED || status == DialogueStatus.INSIGHT_GENERATED
    }

    /**
     * 是否已放弃
     */
    fun isAbandoned(): Boolean {
        return status == DialogueStatus.ABANDONED
    }

    /**
     * 是否进行中
     */
    fun isInProgress(): Boolean {
        return status == DialogueStatus.IN_PROGRESS
    }

    /**
     * 获取状态描述
     */
    fun getStatusDescription(): String {
        return when (status) {
            DialogueStatus.IN_PROGRESS -> "进行中 (${currentRound}/${maxRounds}轮)"
            DialogueStatus.COMPLETED -> "已完成"
            DialogueStatus.ABANDONED -> "已放弃"
            DialogueStatus.INSIGHT_GENERATED -> "洞察已生成"
        }
    }

    /**
     * 获取简短状态描述
     */
    fun getShortStatus(): String {
        return when (status) {
            DialogueStatus.IN_PROGRESS -> "进行中"
            DialogueStatus.COMPLETED -> "已完成"
            DialogueStatus.ABANDONED -> "已放弃"
            DialogueStatus.INSIGHT_GENERATED -> "已完成"
        }
    }
}

/**
 * 对话状态枚举
 */
enum class DialogueStatus(val description: String) {
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    ABANDONED("已放弃"),
    INSIGHT_GENERATED("洞察已生成");

    companion object {
        fun fromString(value: String): DialogueStatus {
            return values().find { it.name == value } ?: IN_PROGRESS
        }
    }
}

/**
 * 对话统计
 */
data class DialogueStats(
    val totalDialogues: Long,
    val completedDialogues: Long,
    val inProgressDialogues: Long,
    val averageRounds: Double,
    val totalMessages: Long,
    val averageDepthLevel: Double?
) {
    /**
     * 完成率
     */
    fun getCompletionRate(): Float {
        return if (totalDialogues > 0) {
            completedDialogues.toFloat() / totalDialogues
        } else {
            0f
        }
    }

    /**
     * 深度等级显示
     */
    fun getDepthLevelDisplay(): String {
        return averageDepthLevel?.let {
            String.format("%.1f", it)
        } ?: "-"
    }
}

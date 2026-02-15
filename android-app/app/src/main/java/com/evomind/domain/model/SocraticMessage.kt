package com.evomind.domain.model

import java.time.LocalDateTime

/**
 * 苏格拉底式对话消息
 */
data class SocraticMessage(
    val id: Long,
    val dialogueId: Long,
    val round: Int,
    val role: MessageRole,
    val content: String,
    val type: MessageType,
    val typeDescription: String?,
    val depthLevel: Int,
    val isFollowUp: Boolean,
    val thinkingHints: String?,
    val aiAnalysis: String?,
    val keyPointsExtracted: String?,
    val isFinalSummary: Boolean,
    val sequenceNumber: Int,
    val createdAt: LocalDateTime,
    val followUpOptions: List<FollowUpOption>?
) {
    /**
     * 是否来自AI
     */
    fun isFromAi(): Boolean = role == MessageRole.AI

    /**
     * 是否来自用户
     */
    fun isFromUser(): Boolean = role == MessageRole.USER

    /**
     * 获取内容预览
     */
    fun getPreview(maxLength: Int = 100): String {
        return if (content.length <= maxLength) {
            content
        } else {
            content.take(maxLength) + "..."
        }
    }

    /**
     * 获取深度等级描述
     */
    fun getDepthDescription(): String {
        return when (depthLevel) {
            1 -> "表层"
            2 -> "初步深入"
            3 -> "中度深入"
            4 -> "深度探索"
            5 -> "核心追问"
            else -> "未知"
        }
    }

    /**
     * 格式化时间显示
     */
    fun getTimeDisplay(): String {
        return createdAt.toLocalTime().toString()
    }

    companion object {
        /**
         * 创建用户消息（本地）
         */
        fun createUserMessage(
            dialogueId: Long,
            content: String,
            round: Int
        ): SocraticMessage {
            return SocraticMessage(
                id = 0, // 临时ID
                dialogueId = dialogueId,
                round = round,
                role = MessageRole.USER,
                content = content,
                type = MessageType.USER_RESPONSE,
                typeDescription = "用户回应",
                depthLevel = 1,
                isFollowUp = false,
                thinkingHints = null,
                aiAnalysis = null,
                keyPointsExtracted = null,
                isFinalSummary = false,
                sequenceNumber = 0,
                createdAt = LocalDateTime.now(),
                followUpOptions = null
            )
        }
    }
}

/**
 * 追问选项
 */
data class FollowUpOption(
    val id: String,
    val label: String,
    val type: String
)

/**
 * 消息角色
 */
enum class MessageRole(val description: String) {
    AI("AI引导者"),
    USER("我");

    companion object {
        fun fromString(value: String): MessageRole {
            return values().find { it.name == value } ?: AI
        }
    }
}

/**
 * 消息类型
 */
enum class MessageType(val description: String, val isQuestion: Boolean) {
    INITIAL_QUESTION("初始问题", true),
    FOLLOW_UP_QUESTION("追问", true),
    CLARIFYING_QUESTION("澄清问题", true),
    DEEPENING_QUESTION("深化问题", true),
    CHALLENGE_QUESTION("挑战问题", true),
    REFLECTIVE_QUESTION("反思问题", true),
    USER_RESPONSE("我的回应", false),
    USER_QUESTION("我的提问", false),
    INSIGHT_SUMMARY("洞察总结", false),
    TRANSITION("过渡引导", false),
    ENCOURAGEMENT("鼓励", false);

    companion object {
        fun fromString(value: String): MessageType {
            return values().find { it.name == value } ?: USER_RESPONSE
        }
    }
}

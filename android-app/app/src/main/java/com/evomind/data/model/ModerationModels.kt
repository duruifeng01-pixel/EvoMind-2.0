package com.evomind.data.model

import com.google.gson.annotations.SerializedName

/**
 * 内容审核相关数据模型
 */

/**
 * 内容类型
 */
enum class ContentType {
    @SerializedName("CARD_AI_SUMMARY")
    CARD_AI_SUMMARY,
    
    @SerializedName("CARD_AI_INSIGHT")
    CARD_AI_INSIGHT,
    
    @SerializedName("CARD_AI_MIND_MAP")
    CARD_AI_MIND_MAP,
    
    @SerializedName("SOCRATIC_DIALOGUE")
    SOCRATIC_DIALOGUE,
    
    @SerializedName("USER_CORPUS")
    USER_CORPUS,
    
    @SerializedName("VOICE_NOTE")
    VOICE_NOTE,
    
    @SerializedName("USER_COMMENT")
    USER_COMMENT,
    
    @SerializedName("USER_PROFILE")
    USER_PROFILE,
    
    @SerializedName("CHAT_MESSAGE")
    CHAT_MESSAGE,
    
    @SerializedName("SYSTEM_MESSAGE")
    SYSTEM_MESSAGE
}

/**
 * 审核状态
 */
enum class ModerationStatus {
    @SerializedName("PENDING")
    PENDING,
    
    @SerializedName("PROCESSING")
    PROCESSING,
    
    @SerializedName("APPROVED")
    APPROVED,
    
    @SerializedName("REJECTED")
    REJECTED,
    
    @SerializedName("NEED_REVIEW")
    NEED_REVIEW,
    
    @SerializedName("ERROR")
    ERROR
}

/**
 * 违规类型
 */
enum class ViolationType {
    @SerializedName("NONE")
    NONE,
    
    @SerializedName("POLITICS")
    POLITICS,
    
    @SerializedName("PORNOGRAPHY")
    PORNOGRAPHY,
    
    @SerializedName("VIOLENCE")
    VIOLENCE,
    
    @SerializedName("TERRORISM")
    TERRORISM,
    
    @SerializedName("GAMBLING")
    GAMBLING,
    
    @SerializedName("FRAUD")
    FRAUD,
    
    @SerializedName("ABUSE")
    ABUSE,
    
    @SerializedName("ADVERTISEMENT")
    ADVERTISEMENT,
    
    @SerializedName("PRIVACY")
    PRIVACY,
    
    @SerializedName("INTELLECTUAL_PROPERTY")
    INTELLECTUAL_PROPERTY,
    
    @SerializedName("SENSITIVE_WORD")
    SENSITIVE_WORD,
    
    @SerializedName("OTHER")
    OTHER
}

/**
 * 审核请求
 */
data class ModerationRequest(
    @SerializedName("contentType")
    val contentType: ContentType,
    
    @SerializedName("contentId")
    val contentId: String? = null,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("contentSummary")
    val contentSummary: String? = null,
    
    @SerializedName("isAiGenerated")
    val isAiGenerated: Boolean = false,
    
    @SerializedName("aiModel")
    val aiModel: String? = null,
    
    @SerializedName("forceReCheck")
    val forceReCheck: Boolean = false,
    
    @SerializedName("highPriority")
    val highPriority: Boolean = false
)

/**
 * 审核响应
 */
data class ModerationResponse(
    @SerializedName("logId")
    val logId: Long,
    
    @SerializedName("status")
    val status: ModerationStatus,
    
    @SerializedName("statusDescription")
    val statusDescription: String,
    
    @SerializedName("approved")
    val approved: Boolean,
    
    @SerializedName("shouldBlock")
    val shouldBlock: Boolean,
    
    @SerializedName("violationType")
    val violationType: ViolationType? = null,
    
    @SerializedName("violationDetails")
    val violationDetails: String? = null,
    
    @SerializedName("hitSensitiveWords")
    val hitSensitiveWords: List<HitWordInfo>? = null,
    
    @SerializedName("moderationType")
    val moderationType: String? = null,
    
    @SerializedName("provider")
    val provider: String? = null,
    
    @SerializedName("needManualReview")
    val needManualReview: Boolean = false,
    
    @SerializedName("suggestedAction")
    val suggestedAction: SuggestedAction? = null,
    
    @SerializedName("moderatedAt")
    val moderatedAt: String? = null,
    
    @SerializedName("processTimeMs")
    val processTimeMs: Long? = null
) {
    /**
     * 建议操作
     */
    enum class SuggestedAction {
        @SerializedName("ALLOW")
        ALLOW,
        
        @SerializedName("BLOCK")
        BLOCK,
        
        @SerializedName("MASK")
        MASK,
        
        @SerializedName("REVIEW")
        REVIEW,
        
        @SerializedName("RETRY")
        RETRY
    }
}

/**
 * 敏感词命中信息
 */
data class HitWordInfo(
    @SerializedName("wordId")
    val wordId: Long,
    
    @SerializedName("word")
    val word: String,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("level")
    val level: String,
    
    @SerializedName("positions")
    val positions: List<Position>
) {
    data class Position(
        @SerializedName("start")
        val start: Int,
        
        @SerializedName("end")
        val end: Int
    )
}

/**
 * 快速检测请求
 */
data class QuickCheckRequest(
    @SerializedName("content")
    val content: String
)

/**
 * 快速检测响应
 */
data class QuickCheckResponse(
    @SerializedName("hasSensitiveWord")
    val hasSensitiveWord: Boolean,
    
    @SerializedName("hits")
    val hits: List<HitWordInfo>? = null
)

/**
 * 审核历史记录
 */
data class ModerationLogItem(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("contentType")
    val contentType: ContentType,
    
    @SerializedName("contentSummary")
    val contentSummary: String? = null,
    
    @SerializedName("moderationStatus")
    val moderationStatus: ModerationStatus,
    
    @SerializedName("violationType")
    val violationType: ViolationType? = null,
    
    @SerializedName("isAiGenerated")
    val isAiGenerated: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * 审核统计
 */
data class ModerationStatistics(
    @SerializedName("totalCount")
    val totalCount: Long = 0,
    
    @SerializedName("approvedCount")
    val approvedCount: Long = 0,
    
    @SerializedName("rejectedCount")
    val rejectedCount: Long = 0,
    
    @SerializedName("pendingCount")
    val pendingCount: Long = 0,
    
    @SerializedName("needReviewCount")
    val needReviewCount: Long = 0,
    
    @SerializedName("approvalRate")
    val approvalRate: Double = 0.0,
    
    @SerializedName("rejectionRate")
    val rejectionRate: Double = 0.0
)
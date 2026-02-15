package com.evomind.data.remote.dto.response

import com.evomind.domain.model.CognitiveConflict
import com.evomind.domain.model.ConflictType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 认知冲突响应 DTO
 */
data class CognitiveConflictResponseDto(
    val id: Long,
    val cardId: Long,
    val cardTitle: String,
    val cardViewpoint: String,
    val profileId: Long,
    val topic: String,
    val userBelief: String,
    val conflictType: String,
    val conflictDescription: String,
    val conflictScore: BigDecimal,
    val acknowledged: Boolean,
    val dismissed: Boolean,
    val aiAnalysis: String,
    val createdAt: String
) {
    fun toDomain(): CognitiveConflict {
        return CognitiveConflict(
            id = id,
            cardId = cardId,
            cardTitle = cardTitle,
            cardViewpoint = cardViewpoint,
            profileId = profileId,
            topic = topic,
            userBelief = userBelief,
            conflictType = ConflictType.valueOf(conflictType),
            conflictDescription = conflictDescription,
            conflictScore = conflictScore,
            isAcknowledged = acknowledged,
            isDismissed = dismissed,
            aiAnalysis = aiAnalysis,
            createdAt = LocalDateTime.parse(createdAt)
        )
    }
}

/**
 * 未解决冲突列表响应
 */
data class UnresolvedConflictsResponseDto(
    val conflicts: List<CognitiveConflictResponseDto>,
    val totalCount: Long,
    val hasMore: Boolean
)

/**
 * 用户认知画像 DTO
 */
data class UserCognitiveProfileDto(
    val id: Long,
    val topic: String,
    val coreBelief: String,
    val beliefType: String,
    val confidenceLevel: BigDecimal,
    val evidenceCount: Int
)

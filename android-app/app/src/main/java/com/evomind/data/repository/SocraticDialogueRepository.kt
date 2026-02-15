package com.evomind.data.repository

import com.evomind.data.remote.api.*
import com.evomind.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 苏格拉底式对话Repository
 */
interface SocraticDialogueRepository {
    suspend fun startDialogue(discussionId: Long, initialThought: String?, maxRounds: Int = 5): Result<SocraticDialogue>
    suspend fun sendMessage(dialogueId: Long, content: String): Result<SocraticMessage>
    suspend fun getDialogue(dialogueId: Long): Result<SocraticDialogue>
    suspend fun getDialogueMessages(dialogueId: Long): Result<List<SocraticMessage>>
    suspend fun getUserDialogues(page: Int, size: Int): Result<Pair<List<SocraticDialogue>, Boolean>>
    suspend fun finalizeDialogue(dialogueId: Long, satisfaction: Int?): Result<SocraticInsight>
    suspend fun abandonDialogue(dialogueId: Long): Result<Unit>
    suspend fun getActiveDialogue(discussionId: Long): Result<SocraticDialogue?>
    suspend fun canStartDialogue(discussionId: Long): Result<Boolean>
    suspend fun regenerateResponse(messageId: Long): Result<SocraticMessage>
    suspend fun getDialogueStats(): Result<DialogueStats>
}

@Singleton
class SocraticDialogueRepositoryImpl @Inject constructor(
    private val api: SocraticDialogueApi
) : SocraticDialogueRepository {

    override suspend fun startDialogue(
        discussionId: Long,
        initialThought: String?,
        maxRounds: Int
    ): Result<SocraticDialogue> = try {
        val request = StartSocraticRequestDto(
            discussionId = discussionId,
            maxRounds = maxRounds,
            initialThought = initialThought
        )
        val response = api.startDialogue(request)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "开始对话失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sendMessage(dialogueId: Long, content: String): Result<SocraticMessage> = try {
        val request = SendMessageRequestDto(
            dialogueId = dialogueId,
            content = content
        )
        val response = api.sendMessage(request)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "发送消息失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getDialogue(dialogueId: Long): Result<SocraticDialogue> = try {
        val response = api.getDialogue(dialogueId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取对话失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getDialogueMessages(dialogueId: Long): Result<List<SocraticMessage>> = try {
        val response = api.getDialogueMessages(dialogueId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.map { it.toDomain() })
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取消息失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserDialogues(
        page: Int,
        size: Int
    ): Result<Pair<List<SocraticDialogue>, Boolean>> = try {
        val response = api.getUserDialogues(page, size)
        if (response.isSuccessful && response.body()?.code == 200) {
            val pagedData = response.body()!!.data!!
            val dialogues = pagedData.content.map { it.toDomain() }
            val hasMore = !pagedData.last
            Result.success(dialogues to hasMore)
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取对话列表失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun finalizeDialogue(
        dialogueId: Long,
        satisfaction: Int?
    ): Result<SocraticInsight> = try {
        val response = api.finalizeDialogue(dialogueId, satisfaction)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "生成洞察失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun abandonDialogue(dialogueId: Long): Result<Unit> = try {
        val response = api.abandonDialogue(dialogueId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.body()?.message ?: "放弃对话失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getActiveDialogue(discussionId: Long): Result<SocraticDialogue?> = try {
        val response = api.getActiveDialogue(discussionId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data?.toDomain())
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun canStartDialogue(discussionId: Long): Result<Boolean> = try {
        val response = api.canStartDialogue(discussionId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!)
        } else {
            Result.failure(Exception(response.body()?.message ?: "检查失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun regenerateResponse(messageId: Long): Result<SocraticMessage> = try {
        val response = api.regenerateResponse(messageId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "重新生成失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getDialogueStats(): Result<DialogueStats> = try {
        val response = api.getDialogueStats()
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取统计失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== DTO转换 ====================

    private fun SocraticDialogueResponseDto.toDomain(): SocraticDialogue {
        return SocraticDialogue(
            id = id,
            discussionId = discussionId,
            discussionTitle = discussionTitle,
            status = DialogueStatus.fromString(status),
            currentRound = currentRound,
            maxRounds = maxRounds,
            initialQuestion = initialQuestion,
            finalInsight = finalInsight,
            totalMessages = totalMessages,
            canContinue = canContinue,
            remainingRounds = remainingRounds,
            lastMessageAt = lastMessageAt?.let { parseDateTime(it) },
            createdAt = parseDateTime(createdAt),
            lastMessage = lastMessage?.toDomain()
        )
    }

    private fun SocraticMessageResponseDto.toDomain(): SocraticMessage {
        return SocraticMessage(
            id = id,
            dialogueId = dialogueId,
            round = round,
            role = MessageRole.fromString(role),
            content = content,
            type = MessageType.fromString(type),
            typeDescription = typeDescription,
            depthLevel = depthLevel,
            isFollowUp = isFollowUp,
            thinkingHints = thinkingHints,
            aiAnalysis = aiAnalysis,
            keyPointsExtracted = keyPointsExtracted,
            isFinalSummary = isFinalSummary,
            sequenceNumber = sequenceNumber,
            createdAt = parseDateTime(createdAt),
            followUpOptions = followUpOptions?.map { it.toDomain() }
        )
    }

    private fun FollowUpOptionDto.toDomain(): FollowUpOption {
        return FollowUpOption(
            id = id,
            label = label,
            type = type
        )
    }

    private fun SocraticInsightResponseDto.toDomain(): SocraticInsight {
        return SocraticInsight(
            id = id,
            dialogueId = dialogueId,
            discussionId = discussionId,
            coreInsight = coreInsight,
            thinkingEvolution = thinkingEvolution.map { it.toDomain() },
            turningPoints = turningPoints.map { it.toDomain() },
            unresolvedQuestions = unresolvedQuestions,
            reflectionSuggestion = reflectionSuggestion,
            relatedCards = relatedCards.map { it.toDomain() },
            roundStats = roundStats.toDomain(),
            generatedAt = parseDateTime(generatedAt)
        )
    }

    private fun ThinkingEvolutionDto.toDomain(): ThinkingEvolution {
        return ThinkingEvolution(
            stage = stage,
            description = description,
            userThinking = userThinking,
            aiGuidance = aiGuidance
        )
    }

    private fun KeyTurningPointDto.toDomain(): KeyTurningPoint {
        return KeyTurningPoint(
            round = round,
            description = description,
            beforeAfter = beforeAfter
        )
    }

    private fun RelatedCardDto.toDomain(): RelatedCard {
        return RelatedCard(
            cardId = cardId,
            title = title,
            relevanceReason = relevanceReason
        )
    }

    private fun RoundStatsDto.toDomain(): RoundStats {
        return RoundStats(
            totalRounds = totalRounds,
            avgResponseLength = avgResponseLength,
            depthDistribution = depthDistribution,
            thinkingDepthScore = thinkingDepthScore
        )
    }

    private fun DialogueStatsDto.toDomain(): DialogueStats {
        return DialogueStats(
            totalDialogues = totalDialogues,
            completedDialogues = completedDialogues,
            inProgressDialogues = inProgressDialogues,
            averageRounds = averageRounds,
            totalMessages = totalMessages,
            averageDepthLevel = averageDepthLevel
        )
    }

    private fun parseDateTime(dateTimeStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}

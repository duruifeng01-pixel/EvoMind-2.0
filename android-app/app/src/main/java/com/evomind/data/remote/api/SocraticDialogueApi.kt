package com.evomind.data.remote.api

import com.evomind.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 苏格拉底式对话API接口
 */
interface SocraticDialogueApi {

    @POST("api/socratic/start")
    suspend fun startDialogue(
        @Body request: StartSocraticRequestDto
    ): Response<ApiResponse<SocraticDialogueResponseDto>>

    @POST("api/socratic/message")
    suspend fun sendMessage(
        @Body request: SendMessageRequestDto
    ): Response<ApiResponse<SocraticMessageResponseDto>>

    @GET("api/socratic/dialogues/{id}")
    suspend fun getDialogue(
        @Path("id") dialogueId: Long
    ): Response<ApiResponse<SocraticDialogueResponseDto>>

    @GET("api/socratic/dialogues/{id}/messages")
    suspend fun getDialogueMessages(
        @Path("id") dialogueId: Long
    ): Response<ApiResponse<List<SocraticMessageResponseDto>>>

    @GET("api/socratic/dialogues")
    suspend fun getUserDialogues(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<PagedResponse<SocraticDialogueResponseDto>>>

    @POST("api/socratic/dialogues/{id}/finalize")
    suspend fun finalizeDialogue(
        @Path("id") dialogueId: Long,
        @Query("satisfaction") satisfaction: Int? = null
    ): Response<ApiResponse<SocraticInsightResponseDto>>

    @POST("api/socratic/dialogues/{id}/abandon")
    suspend fun abandonDialogue(
        @Path("id") dialogueId: Long
    ): Response<ApiResponse<Unit>>

    @GET("api/socratic/discussions/{discussionId}/active")
    suspend fun getActiveDialogue(
        @Path("discussionId") discussionId: Long
    ): Response<ApiResponse<SocraticDialogueResponseDto>>

    @GET("api/socratic/discussions/{discussionId}/can-start")
    suspend fun canStartDialogue(
        @Path("discussionId") discussionId: Long
    ): Response<ApiResponse<Boolean>>

    @POST("api/socratic/messages/{messageId}/regenerate")
    suspend fun regenerateResponse(
        @Path("messageId") messageId: Long
    ): Response<ApiResponse<SocraticMessageResponseDto>>

    @GET("api/socratic/stats")
    suspend fun getDialogueStats(): Response<ApiResponse<DialogueStatsDto>>

    @POST("api/socratic/dialogues/{id}/save-as-card")
    suspend fun saveInsightAsCard(
        @Path("id") dialogueId: Long
    ): Response<ApiResponse<Long>>
}

/**
 * 开始对话请求DTO
 */
data class StartSocraticRequestDto(
    val discussionId: Long,
    val maxRounds: Int? = 5,
    val initialThought: String? = null,
    val topicOverride: String? = null
)

/**
 * 发送消息请求DTO
 */
data class SendMessageRequestDto(
    val dialogueId: Long,
    val content: String,
    val requestedDepth: Int? = null,
    val requestFinalize: Boolean = false,
    val satisfaction: Int? = null
)

/**
 * 对话响应DTO
 */
data class SocraticDialogueResponseDto(
    val id: Long,
    val discussionId: Long,
    val discussionTitle: String?,
    val status: String,
    val currentRound: Int,
    val maxRounds: Int,
    val initialQuestion: String?,
    val finalInsight: String?,
    val totalMessages: Int,
    val canContinue: Boolean,
    val remainingRounds: Int,
    val lastMessageAt: String?,
    val createdAt: String,
    val lastMessage: SocraticMessageResponseDto?
)

/**
 * 消息响应DTO
 */
data class SocraticMessageResponseDto(
    val id: Long,
    val dialogueId: Long,
    val round: Int,
    val role: String,
    val content: String,
    val type: String,
    val typeDescription: String?,
    val depthLevel: Int,
    val isFollowUp: Boolean,
    val thinkingHints: String?,
    val aiAnalysis: String?,
    val keyPointsExtracted: String?,
    val isFinalSummary: Boolean,
    val sequenceNumber: Int,
    val createdAt: String,
    val followUpOptions: List<FollowUpOptionDto>?
)

/**
 * 追问选项DTO
 */
data class FollowUpOptionDto(
    val id: String,
    val label: String,
    val type: String
)

/**
 * 洞察响应DTO
 */
data class SocraticInsightResponseDto(
    val id: Long,
    val dialogueId: Long,
    val discussionId: Long,
    val coreInsight: String,
    val thinkingEvolution: List<ThinkingEvolutionDto>,
    val turningPoints: List<KeyTurningPointDto>,
    val unresolvedQuestions: List<String>,
    val reflectionSuggestion: String,
    val relatedCards: List<RelatedCardDto>,
    val roundStats: RoundStatsDto,
    val generatedAt: String
)

/**
 * 思考演变DTO
 */
data class ThinkingEvolutionDto(
    val stage: Int,
    val description: String,
    val userThinking: String,
    val aiGuidance: String
)

/**
 * 转折点DTO
 */
data class KeyTurningPointDto(
    val round: Int,
    val description: String,
    val beforeAfter: String
)

/**
 * 相关卡片DTO
 */
data class RelatedCardDto(
    val cardId: Long,
    val title: String,
    val relevanceReason: String
)

/**
 * 轮次统计DTO
 */
data class RoundStatsDto(
    val totalRounds: Int,
    val avgResponseLength: Int,
    val depthDistribution: List<Int>,
    val thinkingDepthScore: Int
)

/**
 * 对话统计DTO
 */
data class DialogueStatsDto(
    val totalDialogues: Long,
    val completedDialogues: Long,
    val inProgressDialogues: Long,
    val averageRounds: Double,
    val totalMessages: Long,
    val averageDepthLevel: Double?
)

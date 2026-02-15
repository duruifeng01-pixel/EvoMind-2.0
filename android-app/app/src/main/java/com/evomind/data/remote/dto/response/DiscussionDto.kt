package com.evomind.data.remote.dto.response

data class DiscussionDto(
    val id: Long?,
    val userId: Long?,
    val userName: String?,
    val userAvatar: String?,
    val title: String?,
    val content: String?,
    val dailyQuestionId: Long?,
    val isFinalized: Boolean?,
    val replyCount: Int?,
    val likeCount: Int?,
    val createdAt: String?,
    val updatedAt: String?
)

data class DailyQuestionDto(
    val id: Long?,
    val question: String?,
    val context: String?,
    val generationStatus: String?,
    val discussionCount: Int?,
    val createdAt: String?
)

data class CommentDto(
    val id: Long?,
    val discussionId: Long?,
    val userId: Long?,
    val userName: String?,
    val userAvatar: String?,
    val content: String?,
    val likeCount: Int?,
    val isLiked: Boolean?,
    val createdAt: String?
)

data class ReplyDto(
    val id: Long?,
    val discussionId: Long?,
    val userId: Long?,
    val userName: String?,
    val userAvatar: String?,
    val content: String?,
    val createdAt: String?
)

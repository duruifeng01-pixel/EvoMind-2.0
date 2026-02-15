package com.evomind.data.remote.dto.response

data class StudyGroupDto(
    val id: Long?,
    val name: String?,
    val description: String?,
    val avatar: String?,
    val memberCount: Int?,
    val creatorId: Long?,
    val creatorName: String?,
    val isPublic: Boolean?,
    val createdAt: String?
)

data class GroupMemberDto(
    val id: Long?,
    val groupId: Long?,
    val userId: Long?,
    val userName: String?,
    val userAvatar: String?,
    val role: String?,
    val joinedAt: String?
)

data class GroupPostDto(
    val id: Long?,
    val groupId: Long?,
    val userId: Long?,
    val userName: String?,
    val userAvatar: String?,
    val content: String?,
    val likeCount: Int?,
    val commentCount: Int?,
    val isPinned: Boolean?,
    val createdAt: String?
)

data class GroupDiscussionDto(
    val id: Long?,
    val groupId: Long?,
    val title: String?,
    val content: String?,
    val authorId: Long?,
    val authorName: String?,
    val replyCount: Int?,
    val lastReplyAt: String?,
    val createdAt: String?
)

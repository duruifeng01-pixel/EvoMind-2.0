package com.evomind.data.remote.dto.response

data class UserWorkDto(
    val id: Long?,
    val userId: Long?,
    val title: String?,
    val description: String?,
    val workType: String?,
    val fileUrl: String?,
    val thumbnailUrl: String?,
    val badge: BadgeDto?,
    val likeCount: Int?,
    val viewCount: Int?,
    val createdAt: String?
)

data class BadgeDto(
    val id: Long?,
    val name: String?,
    val description: String?,
    val icon: String?,
    val earnedAt: String?
)

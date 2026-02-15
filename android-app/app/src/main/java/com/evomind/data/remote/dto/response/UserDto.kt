package com.evomind.data.remote.dto.response

data class UserProfileDto(
    val id: Long?,
    val username: String?,
    val email: String?,
    val phone: String?,
    val avatar: String?,
    val bio: String?,
    val timezone: String?,
    val language: String?,
    val createdAt: String?,
    val lastLoginAt: String?
)

data class UserSettingsDto(
    val userId: Long?,
    val notificationsEnabled: Boolean?,
    val emailNotifications: Boolean?,
    val pushNotifications: Boolean?,
    val darkMode: Boolean?,
    val fontSize: String?,
    val language: String?
)

data class NotificationDto(
    val id: Long?,
    val userId: Long?,
    val type: String?,
    val title: String?,
    val content: String?,
    val data: Map<String, Any>?,
    val isRead: Boolean?,
    val createdAt: String?
)

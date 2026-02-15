package com.evomind.domain.model

data class UserProfile(
    val id: Long = 0,
    val username: String = "",
    val email: String? = null,
    val phone: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val timezone: String = "Asia/Shanghai",
    val language: String = "zh-CN",
    val createdAt: String? = null,
    val lastLoginAt: String? = null
)

data class UserSettings(
    val userId: Long = 0,
    val notificationsEnabled: Boolean = true,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val darkMode: Boolean = false,
    val fontSize: String = "medium",
    val language: String = "zh-CN"
)

data class AppNotification(
    val id: Long = 0,
    val userId: Long = 0,
    val type: String = "",
    val title: String = "",
    val content: String = "",
    val data: Map<String, Any> = emptyMap(),
    val isRead: Boolean = false,
    val createdAt: String = ""
)

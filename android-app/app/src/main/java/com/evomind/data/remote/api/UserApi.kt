package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): Response<ApiResponseDto<UserProfileDto>>

    @PUT("api/v1/users/me")
    suspend fun updateProfile(@Body request: Map<String, Any>): Response<ApiResponseDto<UserProfileDto>>

    @PUT("api/v1/users/me/avatar")
    suspend fun updateAvatar(@Body request: Map<String, String>): Response<ApiResponseDto<UserProfileDto>>

    @GET("api/v1/users/me/settings")
    suspend fun getSettings(): Response<ApiResponseDto<UserSettingsDto>>

    @PUT("api/v1/users/me/settings")
    suspend fun updateSettings(@Body request: Map<String, Any>): Response<ApiResponseDto<UserSettingsDto>>

    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<List<NotificationDto>>>

    @POST("api/v1/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: Long): Response<ApiResponseDto<NotificationDto>>

    @POST("api/v1/notifications/read-all")
    suspend fun markAllAsRead(): Response<ApiResponseDto<Unit>>
}

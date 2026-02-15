package com.evomind.data.remote.api

import com.evomind.data.remote.dto.request.*
import com.evomind.data.remote.dto.response.AuthResponseDto
import com.evomind.data.remote.dto.ApiResponseDto
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    /**
     * 密码登录
     */
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<ApiResponseDto<AuthResponseDto>>

    /**
     * 验证码登录/注册
     */
    @POST("api/v1/auth/login-by-code")
    suspend fun loginByCode(
        @Body request: PhoneLoginRequestDto
    ): Response<ApiResponseDto<AuthResponseDto>>

    /**
     * 用户注册
     */
    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<ApiResponseDto<AuthResponseDto>>

    /**
     * 发送验证码
     */
    @POST("api/v1/auth/send-verification-code")
    suspend fun sendVerificationCode(
        @Body request: SendCodeRequestDto
    ): Response<ApiResponseDto<Unit>>

    /**
     * 忘记密码申请
     */
    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequestDto
    ): Response<ApiResponseDto<Unit>>

    /**
     * 重置密码
     */
    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequestDto
    ): Response<ApiResponseDto<Unit>>

    /**
     * 刷新Token
     */
    @POST("api/v1/auth/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") authHeader: String
    ): Response<ApiResponseDto<AuthResponseDto>>

    /**
     * 检查手机号是否注册
     */
    @GET("api/v1/auth/check-phone")
    suspend fun checkPhone(
        @Query("phone") phone: String
    ): Response<ApiResponseDto<Boolean>>
}

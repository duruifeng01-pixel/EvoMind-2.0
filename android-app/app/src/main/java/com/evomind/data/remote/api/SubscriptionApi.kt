package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface SubscriptionApi {
    @GET("api/v1/subscriptions/plans")
    suspend fun getPlans(): Response<ApiResponseDto<List<SubscriptionPlanDto>>>

    @GET("api/v1/subscriptions/current")
    suspend fun getCurrentSubscription(): Response<ApiResponseDto<SubscriptionDto>>

    @POST("api/v1/subscriptions")
    suspend fun subscribe(
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<SubscriptionDto>>

    @POST("api/v1/subscriptions/cancel")
    suspend fun cancelSubscription(): Response<ApiResponseDto<SubscriptionDto>>

    @POST("api/v1/subscriptions/upgrade")
    suspend fun upgradePlan(
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<SubscriptionDto>>

    @POST("api/v1/payments/wechat/create")
    suspend fun createWechatOrder(
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<WechatPayParamsDto>>

    @GET("api/v1/payments/{orderNo}")
    suspend fun getPaymentStatus(
        @Path("orderNo") orderNo: String
    ): Response<ApiResponseDto<PaymentOrderDto>>
}

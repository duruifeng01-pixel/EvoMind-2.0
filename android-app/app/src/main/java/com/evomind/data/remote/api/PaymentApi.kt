package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface PaymentApi {
    @POST("api/v1/payment/wechat/create-order")
    suspend fun createWechatOrder(
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<WechatPayOrderDto>>

    @POST("api/v1/payment/alipay/create-order")
    suspend fun createAlipayOrder(
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<AlipayOrderDto>>

    @POST("api/v1/payment/wechat/callback")
    suspend fun wechatCallback(
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<String>>

    @POST("api/v1/payment/alipay/callback")
    suspend fun alipayCallback(
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<String>>

    @GET("api/v1/payment/orders")
    suspend fun getOrders(
        @Query("page") page: Int = 0
    ): Response<ApiResponseDto<List<PaymentOrderDto>>>

    @GET("api/v1/payment/orders/{id}")
    suspend fun getOrder(@Path("id") id: Long): Response<ApiResponseDto<PaymentOrderDto>>

    @POST("api/v1/payment/orders/{id}/refund")
    suspend fun refundOrder(@Path("id") id: Long): Response<ApiResponseDto<PaymentOrderDto>>
}

package com.evomind.data.remote.dto.response

data class SubscriptionPlanDto(
    val id: Long?,
    val name: String?,
    val description: String?,
    val price: Double?,
    val billingCycle: String?,
    val features: List<String>?,
    val isPopular: Boolean?,
    val createdAt: String?
)

data class SubscriptionDto(
    val id: Long?,
    val userId: Long?,
    val planId: Long?,
    val planName: String?,
    val status: String?,
    val startDate: String?,
    val endDate: String?,
    val autoRenew: Boolean?
)

data class PaymentOrderDto(
    val id: Long?,
    val orderNo: String?,
    val userId: Long?,
    val planId: Long?,
    val amount: Double?,
    val currency: String?,
    val paymentMethod: String?,
    val status: String?,
    val createdAt: String?,
    val paidAt: String?
)

data class WechatPayParamsDto(
    val appId: String?,
    val partnerId: String?,
    val prepayId: String?,
    val nonceStr: String?,
    val timeStamp: String?,
    val packageValue: String?,
    val sign: String?
)

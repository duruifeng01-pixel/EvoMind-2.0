package com.evomind.data.remote.dto.response

data class WechatPayOrderDto(
    val orderId: Long?,
    val appId: String?,
    val partnerId: String?,
    val prepayId: String?,
    val nonceStr: String?,
    val timestamp: Long?,
    val sign: String?,
    val packageValue: String?
)

data class AlipayOrderDto(
    val orderId: Long?,
    val orderString: String?
)

data class PaymentOrderDto(
    val id: Long?,
    val userId: Long?,
    val orderNo: String?,
    val amount: Double?,
    val currency: String?,
    val paymentMethod: String?,
    val status: String?,
    val subscriptionPlanId: Long?,
    val createdAt: String?,
    val paidAt: String?
)

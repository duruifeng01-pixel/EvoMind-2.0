package com.evomind.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("password")
    val password: String
)

data class PhoneLoginRequestDto(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("verificationCode")
    val verificationCode: String
)

data class RegisterRequestDto(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("verificationCode")
    val verificationCode: String,
    
    @SerializedName("nickname")
    val nickname: String? = null
)

data class ForgotPasswordRequestDto(
    @SerializedName("phone")
    val phone: String
)

data class ResetPasswordRequestDto(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("verificationCode")
    val verificationCode: String,
    
    @SerializedName("newPassword")
    val newPassword: String
)

data class SendCodeRequestDto(
    @SerializedName("phone")
    val phone: String
)

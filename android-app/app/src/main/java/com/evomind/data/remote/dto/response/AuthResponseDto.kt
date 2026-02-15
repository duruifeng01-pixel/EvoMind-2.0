package com.evomind.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("tokenType")
    val tokenType: String,
    
    @SerializedName("expiresIn")
    val expiresIn: Long,
    
    @SerializedName("user")
    val user: UserInfoDto,
    
    @SerializedName("isNewUser")
    val isNewUser: Boolean = false
)

data class UserInfoDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("nickname")
    val nickname: String?,
    
    @SerializedName("avatar")
    val avatar: String?
)

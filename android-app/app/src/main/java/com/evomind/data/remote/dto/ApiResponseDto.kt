package com.evomind.data.remote.dto

/**
 * API统一响应包装类
 * 与后端 ApiResponse 对应，添加code字段兼容
 */
data class ApiResponseDto<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null,
    val timestamp: String? = null,
    val errorCode: Int? = null
) {
    /**
     * 响应码，200表示成功
     */
    val code: Int
        get() = if (success) 200 else (errorCode ?: 500)
}

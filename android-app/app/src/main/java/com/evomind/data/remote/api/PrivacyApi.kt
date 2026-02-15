package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 隐私与数据权利API接口
 */
interface PrivacyApi {

    /**
     * 导出用户数据
     */
    @POST("api/v1/privacy/export")
    suspend fun exportUserData(): ApiResponse<Map<String, Any>>

    /**
     * 申请注销账号
     */
    @POST("api/v1/privacy/delete-account")
    suspend fun requestAccountDeletion(): ApiResponse<Map<String, String>>

    /**
     * 确认注销账号
     */
    @POST("api/v1/privacy/confirm-deletion")
    suspend fun confirmAccountDeletion(
        @Query("deletionToken") deletionToken: String
    ): ApiResponse<Unit>

    /**
     * 取消注销申请
     */
    @POST("api/v1/privacy/cancel-deletion")
    suspend fun cancelAccountDeletion(): ApiResponse<Unit>

    /**
     * 获取导出历史
     */
    @GET("api/v1/privacy/export-history")
    suspend fun getExportHistory(): ApiResponse<Map<String, Any>>
}

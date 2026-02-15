package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface ShareImageApi {
    @GET("api/v1/share/templates")
    suspend fun getTemplates(): Response<ApiResponseDto<List<ShareTemplateDto>>>

    @POST("api/v1/share/generate")
    suspend fun generateShareImage(
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<GeneratedShareImageDto>>

    @GET("api/v1/share/images")
    suspend fun getMyShareImages(
        @Query("page") page: Int = 0
    ): Response<ApiResponseDto<List<GeneratedShareImageDto>>>
}

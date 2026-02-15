package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.AgentModelResponseDto
import com.evomind.data.remote.dto.response.TrainingProgressDto
import retrofit2.Response
import retrofit2.http.*

interface AgentModelApi {
    @GET("api/v1/agent/models")
    suspend fun getModels(): Response<ApiResponseDto<List<AgentModelResponseDto>>>

    @GET("api/v1/agent/models/{id}")
    suspend fun getModel(@Path("id") id: Long): Response<ApiResponseDto<AgentModelResponseDto>>

    @POST("api/v1/agent/models")
    suspend fun createModel(
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<AgentModelResponseDto>>

    @POST("api/v1/agent/models/{id}/train")
    suspend fun startTraining(
        @Path("id") id: Long,
        @Body config: Map<String, Any>
    ): Response<ApiResponseDto<TrainingProgressDto>>

    @GET("api/v1/agent/models/{id}/progress")
    suspend fun getTrainingProgress(
        @Path("id") id: Long
    ): Response<ApiResponseDto<TrainingProgressDto>>

    @POST("api/v1/agent/models/{id}/stop")
    suspend fun stopTraining(@Path("id") id: Long): Response<ApiResponseDto<AgentModelResponseDto>>

    @DELETE("api/v1/agent/models/{id}")
    suspend fun deleteModel(@Path("id") id: Long): Response<ApiResponseDto<Unit>>

    @POST("api/v1/agent/models/{id}/inference")
    suspend fun runInference(
        @Path("id") id: Long,
        @Body input: Map<String, String>
    ): Response<ApiResponseDto<Map<String, Any>>>
}

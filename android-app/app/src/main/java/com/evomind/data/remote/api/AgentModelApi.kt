package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface AgentModelApi {
    @GET("api/v1/agents")
    suspend fun getAgents(): Response<ApiResponseDto<List<AgentModelDto>>>

    @GET("api/v1/agents/{id}")
    suspend fun getAgent(@Path("id") id: Long): Response<ApiResponseDto<AgentModelDto>>

    @POST("api/v1/agents/train")
    suspend fun trainAgent(@Body request: Map<String, Any>): Response<ApiResponseDto<TrainingJobDto>>

    @GET("api/v1/agents/train/{jobId}/status")
    suspend fun getTrainingStatus(@Path("jobId") jobId: Long): Response<ApiResponseDto<TrainingJobDto>>

    @DELETE("api/v1/agents/{id}")
    suspend fun deleteAgent(@Path("id") id: Long): Response<ApiResponseDto<Unit>>
}

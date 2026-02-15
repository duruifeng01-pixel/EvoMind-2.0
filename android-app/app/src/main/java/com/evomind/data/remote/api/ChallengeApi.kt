package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.ChallengeTaskResponseDto
import com.evomind.data.remote.dto.response.SubmitArtifactRequestDto
import retrofit2.Response
import retrofit2.http.*

interface ChallengeApi {
    @GET("api/v1/challenges/current")
    suspend fun getCurrentChallenge(): Response<ApiResponseDto<ChallengeTaskResponseDto>>

    @GET("api/v1/challenges/{id}")
    suspend fun getChallenge(@Path("id") id: Long): Response<ApiResponseDto<ChallengeTaskResponseDto>>

    @PostMapping("api/v1/challenges/{id}/status")
    suspend fun updateChallengeStatus(
        @Path("id") id: Long,
        @Query("completed") completed: Boolean
    ): Response<ApiResponseDto<ChallengeTaskResponseDto>>

    @POST("api/v1/challenges/{id}/artifact")
    suspend fun submitArtifact(
        @Path("id") id: Long,
        @Body request: SubmitArtifactRequestDto
    ): Response<ApiResponseDto<ChallengeTaskResponseDto>>

    @POST("api/v1/challenges/{id}/claim")
    suspend fun claimReward(@Path("id") id: Long): Response<ApiResponseDto<ChallengeTaskResponseDto>>

    @POST("api/v1/challenges/activity")
    suspend fun recordActivity(@Query("activityType") activityType: String): Response<ApiResponseDto<Unit>>
}

package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface SocraticDialogueApi {
    @GET("api/v1/socratic/dialogues")
    suspend fun getDialogues(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<List<SocraticDialogueDto>>>

    @GET("api/v1/socratic/dialogues/{id}")
    suspend fun getDialogue(@Path("id") id: Long): Response<ApiResponseDto<SocraticDialogueDto>>

    @POST("api/v1/socratic/dialogues")
    suspend fun createDialogue(@Body request: Map<String, Any>): Response<ApiResponseDto<SocraticDialogueDto>>

    @POST("api/v1/socratic/dialogues/{id}/questions")
    suspend fun askQuestion(
        @Path("id") id: Long,
        @Body request: Map<String, String>
    ): Response<ApiResponseDto<QuestionDto>>

    @GET("api/v1/socratic/dialogues/{id}/insights")
    suspend fun getInsights(@Path("id") id: Long): Response<ApiResponseDto<List<SocraticInsightDto>>>

    @POST("api/v1/socratic/dialogues/{id}/generate-insight")
    suspend fun generateInsight(@Path("id") id: Long): Response<ApiResponseDto<SocraticInsightDto>>
}

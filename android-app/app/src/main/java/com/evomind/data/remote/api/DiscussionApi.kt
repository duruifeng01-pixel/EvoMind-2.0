package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.CommentDto
import com.evomind.data.remote.dto.response.DailyQuestionDto
import com.evomind.data.remote.dto.response.DiscussionDto
import com.evomind.data.remote.dto.response.ReplyDto
import retrofit2.Response
import retrofit2.http.*

interface DiscussionApi {
    @GET("api/v1/discussions")
    suspend fun getDiscussions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<List<DiscussionDto>>>

    @GET("api/v1/discussions/{id}")
    suspend fun getDiscussion(
        @Path("id") id: Long
    ): Response<ApiResponseDto<DiscussionDto>>

    @POST("api/v1/discussions/{id}/reply")
    suspend fun replyToDiscussion(
        @Path("id") id: Long,
        @Body content: Map<String, String>
    ): Response<ApiResponseDto<ReplyDto>>

    @PostMapping("/{id}/finalize")
    suspend fun finalizeDiscussion(
        @Path("id") id: Long
    ): Response<ApiResponseDto<DiscussionDto>>

    @GET("api/v1/discussions/{id}/comments")
    suspend fun getComments(
        @Path("id") id: Long
    ): Response<ApiResponseDto<List<CommentDto>>>

    @POST("api/v1/discussions/comments/{commentId}/like")
    suspend fun likeComment(
        @Path("commentId") commentId: Long
    ): Response<ApiResponseDto<CommentDto>>

    @POST("api/v1/discussions/daily-question/generate")
    suspend fun generateDailyQuestion(): Response<ApiResponseDto<DailyQuestionDto>>
}

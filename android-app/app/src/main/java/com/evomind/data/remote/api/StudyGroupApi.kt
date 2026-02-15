package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface StudyGroupApi {
    @GET("api/v1/groups")
    suspend fun getGroups(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<List<StudyGroupDto>>>

    @GET("api/v1/groups/{id}")
    suspend fun getGroup(@Path("id") id: Long): Response<ApiResponseDto<StudyGroupDto>>

    @POST("api/v1/groups")
    suspend fun createGroup(@Body request: Map<String, Any>): Response<ApiResponseDto<StudyGroupDto>>

    @POST("api/v1/groups/{id}/join")
    suspend fun joinGroup(@Path("id") id: Long): Response<ApiResponseDto<GroupMemberDto>>

    @POST("api/v1/groups/{id}/leave")
    suspend fun leaveGroup(@Path("id") id: Long): Response<ApiResponseDto<Unit>>

    @GET("api/v1/groups/{id}/members")
    suspend fun getMembers(@Path("id") id: Long): Response<ApiResponseDto<List<GroupMemberDto>>>

    @GET("api/v1/groups/{id}/posts")
    suspend fun getPosts(
        @Path("id") id: Long,
        @Query("page") page: Int = 0
    ): Response<ApiResponseDto<List<GroupPostDto>>>

    @POST("api/v1/groups/{id}/posts")
    suspend fun createPost(
        @Path("id") id: Long,
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<GroupPostDto>>

    @GET("api/v1/groups/{id}/discussions")
    suspend fun getDiscussions(
        @Path("id") id: Long,
        @Query("page") page: Int = 0
    ): Response<ApiResponseDto<List<GroupDiscussionDto>>>

    @POST("api/v1/groups/{id}/check-in")
    suspend fun checkIn(@Path("id") id: Long): Response<ApiResponseDto<Map<String, Any>>>
}

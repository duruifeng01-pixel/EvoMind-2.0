package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.CreateCorpusRequestDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 用户语料库API接口
 */
interface CorpusApi {

    /**
     * 创建语料
     */
    @POST("api/v1/corpus")
    suspend fun createCorpus(
        @Body request: CreateCorpusRequestDto
    ): Response<ApiResponseDto<UserCorpusResponseDto>>

    /**
     * 获取用户语料库列表
     */
    @GET("api/v1/corpus")
    suspend fun getUserCorpus(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<PagedCorpusResponseDto>>

    /**
     * 获取语料详情
     */
    @GET("api/v1/corpus/{id}")
    suspend fun getCorpusDetail(
        @Path("id") id: Long
    ): Response<ApiResponseDto<UserCorpusDetailResponseDto>>

    /**
     * 获取归档的语料
     */
    @GET("api/v1/corpus/archived")
    suspend fun getArchivedCorpus(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<PagedCorpusResponseDto>>

    /**
     * 获取收藏的语料
     */
    @GET("api/v1/corpus/favorites")
    suspend fun getFavoriteCorpus(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<PagedCorpusResponseDto>>

    /**
     * 按类型获取语料
     */
    @GET("api/v1/corpus/type/{type}")
    suspend fun getCorpusByType(
        @Path("type") type: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<PagedCorpusResponseDto>>

    /**
     * 获取置顶的语料
     */
    @GET("api/v1/corpus/pinned")
    suspend fun getPinnedCorpus(): Response<ApiResponseDto<List<UserCorpusResponseDto>>>

    /**
     * 搜索语料
     */
    @GET("api/v1/corpus/search")
    suspend fun searchCorpus(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<PagedCorpusResponseDto>>

    /**
     * 收藏/取消收藏语料
     */
    @POST("api/v1/corpus/{id}/favorite")
    suspend fun toggleFavorite(
        @Path("id") id: Long
    ): Response<ApiResponseDto<UserCorpusResponseDto>>

    /**
     * 置顶/取消置顶语料
     */
    @POST("api/v1/corpus/{id}/pin")
    suspend fun togglePin(
        @Path("id") id: Long
    ): Response<ApiResponseDto<UserCorpusResponseDto>>

    /**
     * 归档语料
     */
    @POST("api/v1/corpus/{id}/archive")
    suspend fun archiveCorpus(
        @Path("id") id: Long
    ): Response<ApiResponseDto<UserCorpusResponseDto>>

    /**
     * 取消归档语料
     */
    @POST("api/v1/corpus/{id}/unarchive")
    suspend fun unarchiveCorpus(
        @Path("id") id: Long
    ): Response<ApiResponseDto<UserCorpusResponseDto>>

    /**
     * 删除语料
     */
    @DELETE("api/v1/corpus/{id}")
    suspend fun deleteCorpus(
        @Path("id") id: Long
    ): Response<ApiResponseDto<Unit>>

    /**
     * 获取语料统计
     */
    @GET("api/v1/corpus/stats")
    suspend fun getCorpusStats(): Response<ApiResponseDto<CorpusStatsResponseDto>>

    /**
     * 获取讨论相关的洞察
     */
    @GET("api/v1/corpus/discussion/{discussionId}")
    suspend fun getInsightsByDiscussion(
        @Path("discussionId") discussionId: Long
    ): Response<ApiResponseDto<List<UserCorpusResponseDto>>>
}

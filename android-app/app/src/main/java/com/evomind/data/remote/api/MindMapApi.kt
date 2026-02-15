package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.DrilldownResponseDto
import com.evomind.data.remote.dto.response.MindMapResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MindMapApi {
    @GET("api/v1/cards/{id}/mindmap")
    suspend fun getMindMap(
        @Path("id") cardId: Long
    ): Response<ApiResponseDto<MindMapResponseDto>>

    @GET("api/v1/cards/{id}/drilldown")
    suspend fun getDrilldown(
        @Path("id") cardId: Long,
        @Query("nodeId") nodeId: String
    ): Response<ApiResponseDto<DrilldownResponseDto>>
}

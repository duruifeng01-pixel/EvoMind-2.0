package com.evomind.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 冲突响应DTO
 */
data class ConflictResponseDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("cardId1")
    val cardId1: Long,
    
    @SerializedName("cardId2")
    val cardId2: Long,
    
    @SerializedName("cardTitle1")
    val cardTitle1: String?,
    
    @SerializedName("cardTitle2")
    val cardTitle2: String?,
    
    @SerializedName("cardViewpoint1")
    val cardViewpoint1: String?,
    
    @SerializedName("cardViewpoint2")
    val cardViewpoint2: String?,
    
    @SerializedName("conflictType")
    val conflictType: String?,
    
    @SerializedName("conflictDescription")
    val conflictDescription: String?,
    
    @SerializedName("topic")
    val topic: String?,
    
    @SerializedName("similarityScore")
    val similarityScore: BigDecimal?,
    
    @SerializedName("conflictScore")
    val conflictScore: BigDecimal?,
    
    @SerializedName("isAcknowledged")
    val isAcknowledged: Boolean?,
    
    @SerializedName("aiAnalysis")
    val aiAnalysis: String?,
    
    @SerializedName("createdAt")
    val createdAt: LocalDateTime?,
    
    @SerializedName("acknowledgedAt")
    val acknowledgedAt: LocalDateTime?
)

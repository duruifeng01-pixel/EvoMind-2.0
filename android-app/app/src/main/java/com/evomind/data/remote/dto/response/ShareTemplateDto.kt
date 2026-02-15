package com.evomind.data.remote.dto.response

data class ShareTemplateDto(
    val id: Long?,
    val name: String?,
    val templateType: String?,
    val backgroundColor: String?,
    val textColor: String?,
    val elements: List<ShareElementDto>?,
    val createdAt: String?
)

data class ShareElementDto(
    val type: String?,
    val x: Int?,
    val y: Int?,
    val width: Int?,
    val height: Int?,
    val content: String?,
    val style: Map<String, Any>?
)

data class GeneratedShareImageDto(
    val id: Long?,
    val templateId: Long?,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val width: Int?,
    val height: Int?,
    val createdAt: String?
)

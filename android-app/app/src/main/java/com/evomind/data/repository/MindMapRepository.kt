package com.evomind.data.repository

import com.evomind.data.remote.api.MindMapApi
import com.evomind.data.remote.dto.response.DrilldownResponseDto
import com.evomind.data.remote.dto.response.MindMapResponseDto
import com.evomind.domain.model.DrilldownContent
import com.evomind.domain.model.MindMap
import com.evomind.domain.model.MindMapNode
import com.evomind.domain.model.Paragraph
import com.evomind.domain.model.RelatedCard
import javax.inject.Inject
import javax.inject.Singleton

interface MindMapRepository {
    suspend fun getMindMap(cardId: Long): Result<MindMap>
    suspend fun getDrilldown(cardId: Long, nodeId: String): Result<DrilldownContent>
}

@Singleton
class MindMapRepositoryImpl @Inject constructor(
    private val api: MindMapApi
) : MindMapRepository {

    override suspend fun getMindMap(cardId: Long): Result<MindMap> = try {
        val response = api.getMindMap(cardId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取脑图失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getDrilldown(cardId: Long, nodeId: String): Result<DrilldownContent> = try {
        val response = api.getDrilldown(cardId, nodeId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取下钻内容失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun MindMapResponseDto.toDomain(): MindMap {
        return MindMap(
            cardId = cardId ?: 0,
            cardTitle = cardTitle ?: "",
            nodes = nodes?.map { it.toDomainNode() } ?: emptyList(),
            totalNodes = totalNodes ?: 0
        )
    }

    private fun MindMapNodeDto.toDomainNode(): MindMapNode {
        return MindMapNode(
            nodeId = nodeId ?: "",
            parentNodeId = parentNodeId,
            text = text ?: "",
            description = description,
            nodeType = MindMapNode.NodeType.fromString(nodeType),
            level = level ?: 0,
            sortOrder = sortOrder ?: 0,
            hasOriginalReference = hasOriginalReference ?: false,
            originalContentId = originalContentId,
            originalParagraphIndex = originalParagraphIndex,
            isExpanded = isExpanded ?: true,
            children = children?.map { it.toDomainNode() } ?: emptyList()
        )
    }

    private fun DrilldownResponseDto.toDomain(): DrilldownContent {
        return DrilldownContent(
            nodeId = nodeId ?: "",
            nodeText = nodeText ?: "",
            originalContent = originalContent,
            paragraphs = paragraphs?.map { 
                Paragraph(
                    index = it.index ?: 0,
                    content = it.content ?: "",
                    highlightedText = it.highlightedText
                )
            } ?: emptyList(),
            relatedCards = relatedCards?.map {
                RelatedCard(
                    id = it.id ?: 0,
                    title = it.title ?: "",
                    summary = it.summary
                )
            } ?: emptyList()
        )
    }
}

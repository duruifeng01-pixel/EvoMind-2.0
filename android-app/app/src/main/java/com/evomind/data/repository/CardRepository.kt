package com.evomind.data.repository

import com.evomind.data.remote.api.CardApi
import com.evomind.data.remote.dto.response.CardResponseDto
import com.evomind.domain.model.Card
import com.evomind.domain.model.MindMap
import com.evomind.domain.model.DrilldownContent
import javax.inject.Inject
import javax.inject.Singleton

interface CardRepository {
    suspend fun getFeed(page: Int, size: Int): Result<List<Card>>
    suspend fun getCard(cardId: Long): Result<Card>
    suspend fun getMindMap(cardId: Long): Result<MindMap>
    suspend fun getDrilldown(cardId: Long, nodeId: String): Result<DrilldownContent>
    suspend fun toggleFavorite(cardId: Long): Result<Card>
}

@Singleton
class CardRepositoryImpl @Inject constructor(
    private val api: CardApi
) : CardRepository {

    override suspend fun getFeed(page: Int, size: Int): Result<List<Card>> = try {
        val response = api.getFeed(page, size)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data?.map { it.toDomain() } ?: emptyList())
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取卡片流失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCard(cardId: Long): Result<Card> = try {
        val response = api.getCard(cardId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取卡片失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getMindMap(cardId: Long): Result<MindMap> = try {
        val response = api.getMindMap(cardId)
        if (response.isSuccessful && response.body()?.code == 200) {
            val data = response.body()!!.data!!
            Result.success(
                MindMap(
                    cardId = data.cardId ?: 0,
                    cardTitle = data.cardTitle ?: "",
                    nodes = data.nodes?.map { nodeDto ->
                        com.evomind.domain.model.MindMapNode(
                            nodeId = nodeDto.nodeId ?: "",
                            parentNodeId = nodeDto.parentNodeId,
                            text = nodeDto.text ?: "",
                            description = nodeDto.description,
                            nodeType = com.evomind.domain.model.MindMapNode.NodeType.fromString(nodeDto.nodeType),
                            level = nodeDto.level ?: 0,
                            sortOrder = nodeDto.sortOrder ?: 0,
                            hasOriginalReference = nodeDto.hasOriginalReference ?: false,
                            originalContentId = nodeDto.originalContentId,
                            originalParagraphIndex = nodeDto.originalParagraphIndex,
                            isExpanded = nodeDto.isExpanded ?: true,
                            children = nodeDto.children?.map { childDto ->
                                com.evomind.domain.model.MindMapNode(
                                    nodeId = childDto.nodeId ?: "",
                                    parentNodeId = childDto.parentNodeId,
                                    text = childDto.text ?: "",
                                    description = childDto.description,
                                    nodeType = com.evomind.domain.model.MindMapNode.NodeType.fromString(childDto.nodeType),
                                    level = childDto.level ?: 0,
                                    sortOrder = childDto.sortOrder ?: 0,
                                    hasOriginalReference = childDto.hasOriginalReference ?: false,
                                    originalContentId = childDto.originalContentId,
                                    originalParagraphIndex = childDto.originalParagraphIndex,
                                    isExpanded = childDto.isExpanded ?: true
                                )
                            } ?: emptyList()
                        )
                    } ?: emptyList(),
                    totalNodes = data.totalNodes ?: 0
                )
            )
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取脑图失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getDrilldown(cardId: Long, nodeId: String): Result<DrilldownContent> = try {
        val response = api.getDrilldown(cardId, nodeId)
        if (response.isSuccessful && response.body()?.code == 200) {
            val data = response.body()!!.data!!
            Result.success(
                DrilldownContent(
                    nodeId = data.nodeId ?: "",
                    nodeText = data.nodeText ?: "",
                    originalContent = data.originalContent,
                    paragraphs = data.paragraphs?.map {
                        com.evomind.domain.model.Paragraph(
                            index = it.index ?: 0,
                            content = it.content ?: "",
                            highlightedText = it.highlightedText
                        )
                    } ?: emptyList(),
                    relatedCards = data.relatedCards?.map {
                        com.evomind.domain.model.RelatedCard(
                            id = it.id ?: 0,
                            title = it.title ?: "",
                            summary = it.summary
                        )
                    } ?: emptyList()
                )
            )
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取下钻内容失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun toggleFavorite(cardId: Long): Result<Card> = try {
        val response = api.toggleFavorite(cardId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "操作失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun CardResponseDto.toDomain(): Card {
        return Card(
            id = id ?: 0,
            userId = userId ?: 0,
            title = title ?: "",
            summaryText = summaryText,
            oneSentenceSummary = oneSentenceSummary,
            sourceId = sourceId,
            sourceUrl = sourceUrl,
            sourceTitle = sourceTitle,
            originalContentId = originalContentId,
            mindmapJson = mindmapJson,
            isFavorite = isFavorite ?: false,
            isArchived = isArchived ?: false,
            hasConflict = hasConflict ?: false,
            conflictCardIds = conflictCardIds,
            viewCount = viewCount ?: 0,
            lastViewedAt = lastViewedAt
        )
    }
}

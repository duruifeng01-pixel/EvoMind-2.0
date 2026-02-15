package com.evomind.domain.model

data class MindMap(
    val cardId: Long = 0,
    val cardTitle: String = "",
    val nodes: List<MindMapNode> = emptyList(),
    val totalNodes: Int = 0
)

data class MindMapNode(
    val nodeId: String = "",
    val parentNodeId: String? = null,
    val text: String = "",
    val description: String? = null,
    val nodeType: NodeType = NodeType.MAIN,
    val level: Int = 0,
    val sortOrder: Int = 0,
    val hasOriginalReference: Boolean = false,
    val originalContentId: Long? = null,
    val originalParagraphIndex: Int? = null,
    val isExpanded: Boolean = true,
    val children: List<MindMapNode> = emptyList()
) {
    enum class NodeType(val displayName: String) {
        MAIN("核心主题"),
        BRANCH("分支主题"),
        SUB_BRANCH("子主题"),
        LEAF("叶子节点");

        companion object {
            fun fromString(value: String?): NodeType {
                return entries.find { it.name == value } ?: LEAF
            }
        }
    }
}

data class DrilldownContent(
    val nodeId: String = "",
    val nodeText: String = "",
    val originalContent: String? = null,
    val paragraphs: List<Paragraph> = emptyList(),
    val relatedCards: List<RelatedCard> = emptyList()
)

data class Paragraph(
    val index: Int = 0,
    val content: String = "",
    val highlightedText: String? = null
)

data class RelatedCard(
    val id: Long = 0,
    val title: String = "",
    val summary: String? = null
)

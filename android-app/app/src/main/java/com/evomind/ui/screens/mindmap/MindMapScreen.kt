package com.evomind.ui.screens.mindmap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evomind.domain.model.MindMapNode
import com.evomind.ui.viewmodel.MindMapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapScreen(
    cardId: Long,
    cardTitle: String,
    viewModel: MindMapViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cardId) {
        viewModel.loadMindMap(cardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("知识脑图", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = cardTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: 展开/收起所有 */ }) {
                        Icon(Icons.Default.UnfoldMore, contentDescription = "展开全部")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadMindMap(cardId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.mindMap != null -> {
                    MindMapContent(
                        nodes = uiState.mindMap!!.nodes,
                        expandedNodes = uiState.expandedNodes,
                        selectedNode = uiState.selectedNode,
                        onNodeClick = { node ->
                            viewModel.selectNode(node)
                        },
                        onToggleExpand = { nodeId ->
                            viewModel.toggleNode(nodeId)
                        }
                    )
                }
            }
        }
    }

    // Drilldown Bottom Sheet
    if (uiState.drilldownContent != null && uiState.selectedNode != null) {
        DrilldownBottomSheet(
            nodeText = uiState.selectedNode!!.text,
            content = uiState.drilldownContent,
            onDismiss = { viewModel.clearDrilldown() }
        )
    }
}

@Composable
private fun MindMapContent(
    nodes: List<MindMapNode>,
    expandedNodes: Set<String>,
    selectedNode: MindMapNode?,
    onNodeClick: (MindMapNode) -> Unit,
    onToggleExpand: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(nodes, key = { it.nodeId }) { node ->
            MindMapNodeItem(
                node = node,
                isExpanded = node.nodeId in expandedNodes,
                isSelected = selectedNode?.nodeId == node.nodeId,
                expandedNodes = expandedNodes,
                onNodeClick = onNodeClick,
                onToggleExpand = onToggleExpand,
                level = 0
            )
        }
    }
}

@Composable
private fun MindMapNodeItem(
    node: MindMapNode,
    isExpanded: Boolean,
    isSelected: Boolean,
    expandedNodes: Set<String>,
    onNodeClick: (MindMapNode) -> Unit,
    onToggleExpand: (String) -> Unit,
    level: Int
) {
    val indent = level * 20.dp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .clickable { onNodeClick(node) },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                level == 0 -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (level == 0) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand/Collapse button
            if (node.children.isNotEmpty()) {
                IconButton(
                    onClick = { onToggleExpand(node.nodeId) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(24.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Node Type Icon
            Icon(
                imageVector = when (node.nodeType) {
                    MindMapNode.NodeType.MAIN -> Icons.Default.Star
                    MindMapNode.NodeType.BRANCH -> Icons.Default.AccountTree
                    MindMapNode.NodeType.SUB_BRANCH -> Icons.Default.SubdirectoryArrowRight
                    MindMapNode.NodeType.LEAF -> Icons.Default.Circle
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = when (node.nodeType) {
                    MindMapNode.NodeType.MAIN -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Node Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (level == 0) FontWeight.Bold else FontWeight.Normal
                )
                if (!node.description.isNullOrBlank()) {
                    Text(
                        text = node.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Original Reference Indicator
            if (node.hasOriginalReference) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = "有原文引用",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }

    // Children
    AnimatedVisibility(
        visible = isExpanded && node.children.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            node.children.forEach { childNode ->
                MindMapNodeItem(
                    node = childNode,
                    isExpanded = childNode.nodeId in expandedNodes,
                    isSelected = selectedNode?.nodeId == childNode.nodeId,
                    expandedNodes = expandedNodes,
                    onNodeClick = onNodeClick,
                    onToggleExpand = onToggleExpand,
                    level = level + 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrilldownBottomSheet(
    nodeText: String,
    content: com.evomind.domain.model.DrilldownContent?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "原文下钻",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = nodeText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!content?.originalContent.isNullOrBlank()) {
                Text(
                    text = "原文内容",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = content.originalContent,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (content?.paragraphs?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "相关段落",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                content.paragraphs.forEach { paragraph ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "段落 ${paragraph.index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = paragraph.content,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

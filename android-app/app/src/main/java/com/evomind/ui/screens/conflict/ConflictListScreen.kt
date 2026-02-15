package com.evomind.ui.screens.conflict

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.evomind.domain.model.CognitiveConflict
import com.evomind.domain.model.ConflictSeverity
import com.evomind.domain.model.ConflictType

/**
 * 认知冲突列表页面
 * 展示新卡片与用户认知体系的冲突
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CognitiveConflictListScreen(
    conflicts: List<CognitiveConflict>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onConflictClick: (CognitiveConflict) -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("观点冲突提醒") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onRefresh) {
                        Text("刷新")
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (conflicts.isEmpty()) {
                EmptyConflictView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        InfoBanner()
                    }

                    item {
                        Text(
                            text = "发现 ${conflicts.size} 个与您观点不同的内容",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(conflicts) { conflict ->
                        CognitiveConflictListItem(
                            conflict = conflict,
                            onClick = { onConflictClick(conflict) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBanner() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "系统检测到以下新内容与您现有的认知观点存在差异，建议您关注思考",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EmptyConflictView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无观点冲突",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "当新内容与您认知观点不符时，系统会自动提醒",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CognitiveConflictListItem(
    conflict: CognitiveConflict,
    onClick: () -> Unit
) {
    val severity = conflict.getSeverity()
    val severityColor = when (severity) {
        ConflictSeverity.HIGH -> MaterialTheme.colorScheme.error
        ConflictSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
        ConflictSeverity.LOW -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 头部：主题和冲突分数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 主题标签
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = conflict.topic,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 冲突分数
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = severityColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "冲突度",
                            style = MaterialTheme.typography.labelSmall,
                            color = severityColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${(conflict.conflictScore.toFloat() * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = severityColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 新卡片标题
            Text(
                text = "📄 ${conflict.cardTitle}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 您的观点 vs 新观点
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 您的观点
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "您的观点",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = conflict.userBelief,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "vs",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 新观点
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "新内容观点",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = conflict.cardViewpoint,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = severityColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 冲突类型标签
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (conflict.conflictType) {
                    ConflictType.CONTRADICTORY -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ConflictType.CHALLENGING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    ConflictType.DIFFERENT_PERSPECTIVE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = conflict.getConflictTypeDescription(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = when (conflict.conflictType) {
                        ConflictType.CONTRADICTORY -> MaterialTheme.colorScheme.error
                        ConflictType.CHALLENGING -> MaterialTheme.colorScheme.tertiary
                        ConflictType.DIFFERENT_PERSPECTIVE -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

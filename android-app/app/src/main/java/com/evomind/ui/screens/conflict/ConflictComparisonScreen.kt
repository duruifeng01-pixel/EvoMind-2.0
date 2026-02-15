package com.evomind.ui.screens.conflict

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.evomind.domain.model.CognitiveConflict
import com.evomind.domain.model.ConflictSeverity
import com.evomind.domain.model.ConflictType

/**
 * 认知冲突对比页面
 * 展示新卡片观点与用户认知体系的对比
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CognitiveConflictComparisonScreen(
    conflict: CognitiveConflict,
    onBackClick: () -> Unit,
    onAcknowledgeClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("观点冲突详情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (!conflict.isAcknowledged && !conflict.isDismissed) {
                BottomActionBar(
                    onAcknowledgeClick = onAcknowledgeClick,
                    onDismissClick = onDismissClick
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 冲突信息头部
            ConflictHeader(conflict)

            // 观点对比区域
            ViewpointComparisonSection(conflict)

            // 冲突分析
            ConflictAnalysisSection(conflict)

            // AI 深度分析
            AiAnalysisSection(conflict.aiAnalysis)
        }
    }
}

@Composable
private fun BottomActionBar(
    onAcknowledgeClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismissClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("忽略")
            }

            Button(
                onClick = onAcknowledgeClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("已阅，标记冲突")
            }
        }
    }
}

@Composable
private fun ConflictHeader(conflict: CognitiveConflict) {
    val severity = conflict.getSeverity()
    val severityColor = when (severity) {
        ConflictSeverity.HIGH -> MaterialTheme.colorScheme.error
        ConflictSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
        ConflictSeverity.LOW -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = severityColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = severityColor,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "检测到${conflict.getConflictTypeDescription()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "主题: ${conflict.topic}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 冲突分数
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = severityColor.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "冲突强度",
                        style = MaterialTheme.typography.bodyMedium,
                        color = severityColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(conflict.conflictScore.toFloat() * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewpointComparisonSection(conflict: CognitiveConflict) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "观点对比",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 用户观点卡片
        UserBeliefCard(userBelief = conflict.userBelief)

        Spacer(modifier = Modifier.height(12.dp))

        // 冲突指示器
        ConflictIndicator(conflictType = conflict.conflictType)

        Spacer(modifier = Modifier.height(12.dp))

        // 新卡片观点
        NewCardViewpointCard(
            cardTitle = conflict.cardTitle,
            cardViewpoint = conflict.cardViewpoint
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun UserBeliefCard(userBelief: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "您的认知观点",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userBelief,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "基于您语料库中的历史观点",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ConflictIndicator(conflictType: ConflictType) {
    val (icon, text, color) = when (conflictType) {
        ConflictType.CONTRADICTORY -> Triple(
            Icons.Default.Warning,
            "观点对立",
            MaterialTheme.colorScheme.error
        )
        ConflictType.CHALLENGING -> Triple(
            Icons.Default.Warning,
            "挑战您的信念",
            MaterialTheme.colorScheme.tertiary
        )
        ConflictType.DIFFERENT_PERSPECTIVE -> Triple(
            Icons.Default.Psychology,
            "不同视角",
            MaterialTheme.colorScheme.primary
        )
        else -> Triple(
            Icons.Default.Psychology,
            "观点差异",
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = color.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun NewCardViewpointCard(cardTitle: String, cardViewpoint: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "新内容的观点",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📄 $cardTitle",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = cardViewpoint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun ConflictAnalysisSection(conflict: CognitiveConflict) {
    if (conflict.conflictDescription.isBlank()) return

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "冲突分析",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = conflict.conflictDescription,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AiAnalysisSection(analysis: String) {
    if (analysis.isBlank()) return

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "AI 深度分析",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = analysis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 思考引导
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🤔 值得思考的问题",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 为什么我的观点与新内容不同？数据来源有何差异？\n" +
                           "• 这个新视角是否有我未曾考虑过的角度？\n" +
                           "• 我是否需要在认知笔记中记录这个冲突以便后续思考？",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

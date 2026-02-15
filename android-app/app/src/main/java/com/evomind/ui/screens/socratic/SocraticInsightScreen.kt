package com.evomind.ui.screens.socratic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.evomind.domain.model.SocraticInsight
import com.evomind.domain.model.ThinkingEvolution
import com.evomind.domain.model.KeyTurningPoint

/**
 * 苏格拉底式对话洞察详情屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocraticInsightScreen(
    insight: SocraticInsight,
    onNavigateBack: () -> Unit,
    onShare: () -> Unit = {},
    onSaveToCard: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("对话洞察") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 头部概览
            InsightHeader(insight = insight)

            // 核心洞察
            CoreInsightSection(insight = insight)

            // 思考演变
            if (insight.thinkingEvolution.isNotEmpty()) {
                ThinkingEvolutionSection(evolution = insight.thinkingEvolution)
            }

            // 关键转折点
            if (insight.turningPoints.isNotEmpty()) {
                TurningPointsSection(turningPoints = insight.turningPoints)
            }

            // 未解问题
            if (insight.hasUnresolvedQuestions()) {
                UnresolvedQuestionsSection(questions = insight.unresolvedQuestions)
            }

            // 反思建议
            ReflectionSection(suggestion = insight.reflectionSuggestion)

            // 底部操作
            InsightActions(
                onSaveToCard = onSaveToCard,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 洞察头部
 */
@Composable
private fun InsightHeader(insight: SocraticInsight) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 深度评分徽章
            Surface(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = insight.getDepthLevel(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 统计数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InsightStat(
                    value = "${insight.roundStats.totalRounds}",
                    label = "对话轮次"
                )
                InsightStat(
                    value = "${insight.getEvolutionStageCount()}",
                    label = "思考阶段"
                )
                InsightStat(
                    value = "${insight.roundStats.thinkingDepthScore}/10",
                    label = "深度评分"
                )
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun InsightStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}

/**
 * 核心洞察部分
 */
@Composable
private fun CoreInsightSection(insight: SocraticInsight) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        SectionTitle(title = "核心发现", icon = Icons.Default.Lightbulb)

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = insight.coreInsight,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * 思考演变部分
 */
@Composable
private fun ThinkingEvolutionSection(evolution: List<ThinkingEvolution>) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        SectionTitle(title = "思考演变", icon = Icons.Default.TrendingUp)

        Spacer(modifier = Modifier.height(12.dp))

        evolution.forEachIndexed { index, stage ->
            EvolutionStageCard(
                stage = stage,
                isLast = index == evolution.size - 1
            )
            if (index < evolution.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * 演变阶段卡片
 */
@Composable
private fun EvolutionStageCard(stage: ThinkingEvolution, isLast: Boolean) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 阶段标题
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${stage.stage}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stage.description,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 用户思考
            Text(
                text = "你的想法：",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stage.userThinking,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // AI引导
            Text(
                text = "AI引导：",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stage.aiGuidance,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 转折点部分
 */
@Composable
private fun TurningPointsSection(turningPoints: List<KeyTurningPoint>) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        SectionTitle(title = "关键转折", icon = Icons.Default.ChangeCircle)

        Spacer(modifier = Modifier.height(12.dp))

        turningPoints.forEach { point ->
            TurningPointCard(point = point)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * 转折点卡片
 */
@Composable
private fun TurningPointCard(point: KeyTurningPoint) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "第${point.round}轮",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = point.description,
                style = MaterialTheme.typography.bodyMedium
            )

            if (point.beforeAfter.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = point.beforeAfter,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 未解问题部分
 */
@Composable
private fun UnresolvedQuestionsSection(questions: List<String>) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        SectionTitle(title = "值得继续思考的问题", icon = Icons.Default.HelpOutline)

        Spacer(modifier = Modifier.height(12.dp))

        questions.forEachIndexed { index, question ->
            UnresolvedQuestionItem(index = index + 1, question = question)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * 未解问题项
 */
@Composable
private fun UnresolvedQuestionItem(index: Int, question: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.border.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(24.dp)
            )
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 反思建议部分
 */
@Composable
private fun ReflectionSection(suggestion: String) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        SectionTitle(title = "深度反思建议", icon = Icons.Default.SelfImprovement)

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * 部分标题
 */
@Composable
private fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

/**
 * 洞察操作按钮
 */
@Composable
private fun InsightActions(
    onSaveToCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Button(
            onClick = onSaveToCard,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("保存为认知卡片")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "将此洞察保存到你的语料库，方便日后回顾",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

package com.evomind.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilityReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: AbilityReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("能力报告") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { /* TODO: PDF导出 */ }) {
                        Text("导出PDF")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("概览") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("知识图谱") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("成长曲线") }
                )
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    when (selectedTab) {
                        0 -> ReportOverviewTab(uiState.latestReport)
                        1 -> KnowledgeGraphTab(uiState.knowledgeGraph)
                        2 -> GrowthCurveTab(uiState.growthCurve)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportOverviewTab(report: com.evomind.data.remote.dto.response.AbilityReportDto?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            report?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = it.title ?: "能力报告",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it.period ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        it.summary?.let { summary ->
                            Text(text = summary)
                        }
                    }
                }
            }
        }

        item {
            report?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "能力评分",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AbilityScoreRow("知识广度", it.knowledgeBreadth)
                        AbilityScoreRow("知识深度", it.knowledgeDepth)
                        AbilityScoreRow("批判思维", it.criticalThinking)
                        AbilityScoreRow("学习效率", it.learningEfficiency)
                    }
                }
            }
        }

        item {
            report?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "学习数据",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DataRow("认知卡片", "${it.totalCardsRead ?: 0} 张")
                        DataRow("学习时长", "${it.totalLearningHours ?: 0.0} 小时")
                        DataRow("连续学习", "${it.streakDays ?: 0} 天")
                    }
                }
            }
        }

        item {
            report?.let {
                it.stageSuggestions?.let { suggestions ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "阶段建议",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            suggestions.forEach { suggestion ->
                                Text(
                                    text = "• $suggestion",
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AbilityScoreRow(label: String, score: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        score?.let {
            Text(
                text = "$it/100",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(text = value)
    }
}

@Composable
private fun KnowledgeGraphTab(graph: com.evomind.data.remote.dto.response.KnowledgeGraphDto?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (graph != null && !graph.nodes.isNullOrEmpty()) {
            Text("知识图谱可视化区域 (${graph.nodes?.size ?: 0} 个节点)")
        } else {
            Text("暂无知识图谱数据")
        }
    }
}

@Composable
private fun GrowthCurveTab(curve: List<com.evomind.data.remote.dto.response.GrowthCurvePointDto>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (curve.isNotEmpty()) {
            Text("成长曲线图表区域 (${curve.size} 个数据点)")
        } else {
            Text("暂无成长曲线数据")
        }
    }
}

package com.evomind.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GrowthStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: GrowthStatsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("成长数据") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    GrowthStatsContent(
                        stats = uiState.growthStats,
                        abilityProfile = uiState.abilityProfile,
                        evolutionProgress = uiState.evolutionProgress
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthStatsContent(
    stats: com.evomind.data.remote.dto.response.GrowthStatsDto?,
    abilityProfile: com.evomind.data.remote.dto.response.AbilityProfileDto?,
    evolutionProgress: com.evomind.data.remote.dto.response.EvolutionProgressDto?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stats?.let {
            StatCard(title = "学习时长", value = "${it.totalLearningHours ?: 0}小时")
            StatCard(title = "认知卡片", value = "${it.totalCardsRead ?: 0}张")
            StatCard(title = "知识节点", value = "${it.totalMindMapNodes ?: 0}个")
            StatCard(title = "连续学习", value = "${it.streakDays ?: 0}天")
        }

        abilityProfile?.let {
            AbilityProfileCard(profile = it)
        }

        evolutionProgress?.let {
            EvolutionProgressCard(progress = it)
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AbilityProfileCard(profile: com.evomind.data.remote.dto.response.AbilityProfileDto) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "能力画像",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            profile.knowledgeBreadth?.let {
                Text("知识广度: $it")
            }
            profile.knowledgeDepth?.let {
                Text("知识深度: $it")
            }
            profile.criticalThinking?.let {
                Text("批判思维: $it")
            }
        }
    }
}

@Composable
private fun EvolutionProgressCard(progress: com.evomind.data.remote.dto.response.EvolutionProgressDto) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "进化进度",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            progress.currentStage?.let {
                Text("当前阶段: $it")
            }
            progress.progress?.let {
                LinearProgressIndicator(
                    progress = { it / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${it}%")
            }
        }
    }
}

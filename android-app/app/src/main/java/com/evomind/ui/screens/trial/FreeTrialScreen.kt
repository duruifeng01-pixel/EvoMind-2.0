package com.evomind.ui.screens.trial

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeTrialScreen(
    onNavigateBack: () -> Unit,
    viewModel: FreeTrialViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("免费体验") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isActive) {
                TrialActiveContent(
                    remainingDays = uiState.remainingDays,
                    dailyLimit = uiState.dailyLimit,
                    usedToday = uiState.usedToday,
                    remainingToday = uiState.remainingToday
                )
            } else {
                TrialInactiveContent(
                    isLoading = uiState.isLoading,
                    onActivate = { viewModel.activateTrial() }
                )
            }
        }
    }
}

@Composable
private fun TrialActiveContent(
    remainingDays: Int,
    dailyLimit: Int,
    usedToday: Int,
    remainingToday: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "体验已激活",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "剩余 $remainingDays 天",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "今日额度",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { usedToday.toFloat() / dailyLimit.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("已用: $usedToday")
                    Text("剩余: $remainingToday")
                }
            }
        }
    }
}

@Composable
private fun TrialInactiveContent(
    isLoading: Boolean,
    onActivate: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "免费体验",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "激活3天进阶体验，解锁全部高级功能",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Column {
            FeatureItem("每日AI卡片生成额度")
            FeatureItem("知识脑图无限使用")
            FeatureItem("苏格拉底对话无限使用")
            FeatureItem("成长数据可视化")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onActivate,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("立即体验")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "体验结束后可订阅继续使用",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✓", color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

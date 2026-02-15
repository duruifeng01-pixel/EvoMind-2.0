package com.evomind.ui.screens.challenges

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evomind.domain.model.ChallengeTask
import com.evomind.ui.viewmodel.ChallengeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSubmitDialog by remember { mutableStateOf(false) }
    var artifactTitle by remember { mutableStateOf("") }
    var artifactContent by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("今日挑战") }
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadCurrentChallenge() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.currentTask == null -> {
                    EmptyChallengeState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    ChallengeContent(
                        task = uiState.currentTask!!,
                        onMarkComplete = { viewModel.markAsCompleted() },
                        onSubmitArtifact = { showSubmitDialog = true },
                        onClaimReward = { viewModel.claimReward() }
                    )
                }
            }
        }
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("提交作品") },
            text = {
                Column {
                    OutlinedTextField(
                        value = artifactTitle,
                        onValueChange = { artifactTitle = it },
                        label = { Text("作品标题") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = artifactContent,
                        onValueChange = { artifactContent = it },
                        label = { Text("作品内容") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.submitArtifact(artifactTitle, artifactContent)
                        showSubmitDialog = false
                        artifactTitle = ""
                        artifactContent = ""
                    }
                ) {
                    Text("提交")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (uiState.showClaimDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClaimDialog() },
            icon = { Icon(Icons.Default.Celebration, contentDescription = null) },
            title = { Text("任务完成！") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("恭喜完成今日挑战！")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "奖励: ${uiState.currentTask?.rewardPoints} 积分",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.claimReward() }) {
                    Text("领取奖励")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClaimDialog() }) {
                    Text("稍后领取")
                }
            }
        )
    }
}

@Composable
private fun ChallengeContent(
    task: ChallengeTask,
    onMarkComplete: () -> Unit,
    onSubmitArtifact: () -> Unit,
    onClaimReward: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TodayChallengeCard(task = task)
        }

        item {
            ProgressCard(task = task)
        }

        item {
            ActionButtons(
                task = task,
                onMarkComplete = onMarkComplete,
                onSubmitArtifact = onSubmitArtifact,
                onClaimReward = onClaimReward
            )
        }

        item {
            RewardInfoCard(task = task)
        }

        item {
            TipsCard(taskType = task.taskType)
        }
    }
}

@Composable
private fun TodayChallengeCard(task: ChallengeTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "今日挑战",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProgressCard(task: ChallengeTask) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "任务进度",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = task.getProgressText(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { task.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (task.isCompleted) MaterialTheme.colorScheme.tertiary 
                       else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${task.progressPercent}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButtons(
    task: ChallengeTask,
    onMarkComplete: () -> Unit,
    onSubmitArtifact: () -> Unit,
    onClaimReward: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!task.isCompleted) {
            Button(
                onClick = onMarkComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("标记完成")
            }

            if (task.taskType == ChallengeTask.TaskType.SHARE_INSIGHT) {
                OutlinedButton(
                    onClick = onSubmitArtifact,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Create, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("提交作品")
                }
            }
        } else if (!task.rewardClaimed) {
            Button(
                onClick = onClaimReward,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("领取奖励")
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "已完成并领取奖励",
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardInfoCard(task: ChallengeTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Stars, contentDescription = null)
                Text(
                    text = "${task.rewardPoints}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "积分",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CardMembership, contentDescription = null)
                Text(
                    text = "${task.rewardTrialDays}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "体验天数",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun TipsCard(taskType: ChallengeTask.TaskType) {
    val tip = when (taskType) {
        ChallengeTask.TaskType.READ_CARDS -> "阅读认知卡片可以帮助你获取新知识，每张卡片都经过AI智能提炼"
        ChallengeTask.TaskType.ADD_SOURCES -> "添加更多优质信息源，让你的知识视野更加开阔"
        ChallengeTask.TaskType.CREATE_NOTES -> "记录你的思考和灵感，这是知识内化的重要过程"
        ChallengeTask.TaskType.COMPLETE_DISCUSSION -> "参与讨论可以锻炼你的批判性思维"
        ChallengeTask.TaskType.SHARE_INSIGHT -> "分享你的洞见，让更多人受益，也是巩固知识的好方式"
        ChallengeTask.TaskType.DAILY_CHECKIN -> "坚持每日签到，培养良好的学习习惯"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "小贴士",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyChallengeState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "今日任务尚未发布",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "敬请期待明天的挑战",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

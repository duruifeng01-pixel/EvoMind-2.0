package com.evomind.ui.screens.voicenote

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evomind.domain.model.RecordingState
import com.evomind.ui.theme.EvoMindColors

/**
 * 语音笔记录制页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceNoteRecordScreen(
    viewModel: VoiceNoteViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToList: () -> Unit
) {
    val recordingState by viewModel.recordingState.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val statistics by viewModel.statistics.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("语音笔记") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToList) {
                        Icon(Icons.Default.List, contentDescription = "笔记列表")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 统计信息卡片
            StatisticsCard(statistics)

            Spacer(modifier = Modifier.weight(1f))

            // 录音状态和提示
            RecordingStatus(recordingState, recordingDuration)

            Spacer(modifier = Modifier.height(32.dp))

            // 录音按钮
            RecordButton(
                recordingState = recordingState,
                onStartRecording = { viewModel.startRecording() },
                onStopRecording = { viewModel.stopRecording() },
                onCancelRecording = { viewModel.cancelRecording() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 提示文字
            Text(
                text = when (recordingState) {
                    is RecordingState.Idle -> "长按开始录音"
                    is RecordingState.Recording -> "松开结束录音，上滑取消"
                    is RecordingState.Processing -> "正在处理..."
                    is RecordingState.Success -> "保存成功"
                    is RecordingState.Error -> "录音失败"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * 统计信息卡片
 */
@Composable
private fun StatisticsCard(statistics: com.evomind.domain.model.VoiceNoteStatistics?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItem(
                value = statistics?.todayCount?.toString() ?: "0",
                label = "今日记录"
            )
            StatisticItem(
                value = statistics?.totalCount?.toString() ?: "0",
                label = "总记录"
            )
            StatisticItem(
                value = statistics?.formatTotalDuration() ?: "0分钟",
                label = "总时长"
            )
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatisticItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 录音状态显示
 */
@Composable
private fun RecordingStatus(recordingState: RecordingState, duration: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 录音动画
        if (recordingState is RecordingState.Recording) {
            RecordingWaveform()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 时长显示
        AnimatedVisibility(
            visible = recordingState is RecordingState.Recording || recordingState is RecordingState.Processing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.displayMedium,
                color = if (recordingState is RecordingState.Recording) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }

        // 处理进度
        if (recordingState is RecordingState.Processing) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }

        // 错误提示
        if (recordingState is RecordingState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (recordingState as RecordingState.Error).message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        // 成功提示
        if (recordingState is RecordingState.Success) {
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "成功",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * 录音波形动画
 */
@Composable
private fun RecordingWaveform() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val animation by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500 + index * 100, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$index"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((20 + 30 * animation).dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * 录音按钮
 */
@Composable
private fun RecordButton(
    recordingState: RecordingState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit
) {
    val isRecording = recordingState is RecordingState.Recording
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val buttonColor = when (recordingState) {
        is RecordingState.Recording -> MaterialTheme.colorScheme.error
        is RecordingState.Processing -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        is RecordingState.Success -> Color(0xFF4CAF50)
        is RecordingState.Error -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (recordingState is RecordingState.Idle || recordingState is RecordingState.Success || recordingState is RecordingState.Error) {
                            onStartRecording()
                            tryAwaitRelease()
                            if (recordingState is RecordingState.Recording) {
                                onStopRecording()
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 外圈
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = buttonColor.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )

        // 内圈
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = buttonColor,
                    shape = if (isRecording) RoundedCornerShape(8.dp) else CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isRecording -> Icons.Default.Stop
                    recordingState is RecordingState.Processing -> Icons.Default.HourglassEmpty
                    recordingState is RecordingState.Success -> Icons.Default.Check
                    else -> Icons.Default.Mic
                },
                contentDescription = if (isRecording) "停止录音" else "开始录音",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/**
 * 格式化时长
 */
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

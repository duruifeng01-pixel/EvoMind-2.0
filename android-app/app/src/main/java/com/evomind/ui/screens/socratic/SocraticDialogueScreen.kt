package com.evomind.ui.screens.socratic

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evomind.domain.model.*
import com.evomind.ui.viewmodel.SocraticDialogueViewModel
import com.evomind.ui.viewmodel.SocraticUiState
import kotlinx.coroutines.launch

/**
 * 苏格拉底式对话屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocraticDialogueScreen(
    discussionId: Long,
    discussionTitle: String,
    onNavigateBack: () -> Unit,
    onNavigateToInsight: (Long) -> Unit = {},
    viewModel: SocraticDialogueViewModel = hiltViewModel()
) {
    val currentDialogue by viewModel.currentDialogue
    val messages = viewModel.messages
    val uiState by viewModel.uiState
    val inputText by viewModel.inputText
    val canSend by viewModel.canSend
    val insight by viewModel.insight
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 检查是否有活动对话
    LaunchedEffect(discussionId) {
        viewModel.checkCanStart(discussionId) { canStart, existingDialogue ->
            if (existingDialogue != null) {
                // 有活动对话，继续
            } else if (canStart) {
                // 可以开始新对话
                viewModel.startDialogue(discussionId)
            }
        }
    }

    // 滚动到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "苏格拉底式对话",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = discussionTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState is SocraticUiState.Active) {
                            // 显示确认对话框
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 轮次指示器
                    currentDialogue?.let { dialogue ->
                        if (dialogue.isInProgress()) {
                            Text(
                                text = "${dialogue.currentRound}/${dialogue.maxRounds}轮",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            when (uiState) {
                is SocraticUiState.Active, is SocraticUiState.ReadyToFinalize -> {
                    SocraticInputBar(
                        inputText = inputText,
                        canSend = canSend,
                        onInputChange = viewModel::updateInput,
                        onSend = viewModel::sendMessage,
                        onFinalize = {
                            viewModel.finalizeDialogue()
                        },
                        showFinalize = uiState is SocraticUiState.ReadyToFinalize ||
                                (currentDialogue?.currentRound ?: 0) >= 3
                    )
                }
                is SocraticUiState.InsightReady -> {
                    InsightBottomBar(
                        onViewInsight = {
                            insight?.let { onNavigateToInsight(it.id) }
                        },
                        onFinish = onNavigateBack
                    )
                }
                else -> {}
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is SocraticUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SocraticUiState.Error -> {
                    ErrorView(
                        message = (uiState as SocraticUiState.Error).message,
                        onRetry = { viewModel.loadDialogue(currentDialogue?.id ?: 0) }
                    )
                }
                is SocraticUiState.Thinking -> {
                    MessageList(
                        messages = messages,
                        listState = listState,
                        modifier = Modifier.fillMaxSize()
                    )
                    ThinkingIndicator(
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
                is SocraticUiState.GeneratingInsight -> {
                    GeneratingInsightView()
                }
                is SocraticUiState.InsightReady -> {
                    InsightPreview(
                        insight = insight,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    MessageList(
                        messages = messages,
                        listState = listState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * 消息列表
 */
@Composable
private fun MessageList(
    messages: List<SocraticMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        items(messages, key = { it.id }) { message ->
            MessageBubble(message = message)
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * 消息气泡
 */
@Composable
private fun MessageBubble(message: SocraticMessage) {
    val isAi = message.isFromAi()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
    ) {
        if (isAi) {
            // AI头像
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isAi) Alignment.Start else Alignment.End,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // 深度标签（仅AI追问）
            if (isAi && message.isFollowUp) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            "追问 · ${message.getDepthDescription()}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // 消息内容
            Surface(
                color = if (isAi) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                shape = RoundedCornerShape(
                    topStart = if (isAi) 4.dp else 16.dp,
                    topEnd = if (isAi) 16.dp else 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // 思考提示（仅AI消息）
                    message.thinkingHints?.let { hints ->
                        if (hints.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💭 $hints",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // 时间戳
            Text(
                text = message.getTimeDisplay(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (!isAi) {
            Spacer(modifier = Modifier.width(8.dp))
            // 用户头像
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "我",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 输入栏
 */
@Composable
private fun SocraticInputBar(
    inputText: String,
    canSend: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onFinalize: () -> Unit,
    showFinalize: Boolean
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 结束对话按钮（条件显示）
            if (showFinalize) {
                TextButton(
                    onClick = onFinalize,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("生成洞察总结")
                }
            }

            // 输入框
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = { Text("分享你的想法...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                trailingIcon = {
                    IconButton(
                        onClick = onSend,
                        enabled = canSend
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "发送",
                            tint = if (canSend) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            )

            // 字数提示
            Text(
                text = "${inputText.length}/2000",
                style = MaterialTheme.typography.labelSmall,
                color = if (inputText.length > 1800) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * AI思考指示器
 */
@Composable
private fun ThinkingIndicator(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AI正在思考如何引导你...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 错误视图
 */
@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

/**
 * 生成洞察视图
 */
@Composable
private fun GeneratingInsightView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "正在生成深度洞察...",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI正在分析你们的对话，提取关键思考",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 洞察预览
 */
@Composable
private fun InsightPreview(
    insight: SocraticInsight?,
    modifier: Modifier = Modifier
) {
    if (insight == null) return

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        // 标题
        Text(
            text = "对话洞察",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 核心洞察
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "核心发现",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = insight.coreInsight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 思考深度
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "对话轮次",
                value = "${insight.roundStats.totalRounds}轮"
            )
            StatItem(
                label = "思考深度",
                value = insight.getDepthLevel()
            )
            StatItem(
                label = "探索阶段",
                value = "${insight.getEvolutionStageCount()}个"
            )
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 洞察底部栏
 */
@Composable
private fun InsightBottomBar(
    onViewInsight: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onFinish,
                modifier = Modifier.weight(1f)
            ) {
                Text("稍后查看")
            }
            Button(
                onClick = onViewInsight,
                modifier = Modifier.weight(1f)
            ) {
                Text("查看完整洞察")
            }
        }
    }
}

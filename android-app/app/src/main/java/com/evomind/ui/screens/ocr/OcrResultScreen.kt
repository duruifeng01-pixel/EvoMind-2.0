package com.evomind.ui.screens.ocr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.evomind.domain.model.DetectedBlogger
import com.evomind.domain.model.OcrResult
import com.evomind.ui.theme.EvoMindColors

/**
 * OCR结果页面 - 展示识别到的博主列表并支持选择导入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrResultScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit,
    viewModel: OcrResultViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedBloggers by remember { mutableStateOf<Set<String>>(emptySet()) }

    // 加载识别结果
    LaunchedEffect(taskId) {
        viewModel.loadResult(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("识别结果") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EvoMindColors.Surface
                )
            )
        },
        containerColor = EvoMindColors.Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is OcrResultUiState.Loading -> {
                    LoadingContent()
                }
                is OcrResultUiState.Success -> {
                    ResultContent(
                        result = state.result,
                        selectedBloggers = selectedBloggers,
                        onSelectionChange = { id, selected ->
                            selectedBloggers = if (selected) {
                                selectedBloggers + id
                            } else {
                                selectedBloggers - id
                            }
                        },
                        onSelectAll = {
                            selectedBloggers = state.result.bloggers
                                .filter { !it.alreadyExists }
                                .map { it.candidateId }
                                .toSet()
                        },
                        onClearAll = {
                            selectedBloggers = emptySet()
                        }
                    )
                }
                is OcrResultUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadResult(taskId) }
                    )
                }
                else -> {}
            }

            // 底部导入按钮
            if (uiState is OcrResultUiState.Success) {
                ImportButtonBar(
                    selectedCount = selectedBloggers.size,
                    onImport = {
                        viewModel.confirmImport(taskId, selectedBloggers.toList()) {
                            onImportComplete()
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = EvoMindColors.Primary
            )
            Text(
                "正在分析截图...",
                style = MaterialTheme.typography.bodyMedium,
                color = EvoMindColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ResultContent(
    result: OcrResult,
    selectedBloggers: Set<String>,
    onSelectionChange: (String, Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 统计信息
        ResultHeader(
            totalCount = result.bloggers.size,
            newCount = result.bloggers.count { !it.alreadyExists },
            existingCount = result.bloggers.count { it.alreadyExists },
            selectedCount = selectedBloggers.size,
            onSelectAll = onSelectAll,
            onClearAll = onClearAll
        )

        // 博主列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(result.bloggers) { blogger ->
                BloggerCard(
                    blogger = blogger,
                    isSelected = blogger.candidateId in selectedBloggers,
                    onSelectionChange = { selected ->
                        onSelectionChange(blogger.candidateId, selected)
                    }
                )
            }

            // 底部空间（给导入按钮留位置）
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun ResultHeader(
    totalCount: Int,
    newCount: Int,
    existingCount: Int,
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = EvoMindColors.Surface
        )
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
                    text = "识别到 $totalCount 个博主",
                    style = MaterialTheme.typography.titleMedium,
                    color = EvoMindColors.TextPrimary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onSelectAll) {
                        Text("全选", color = EvoMindColors.Primary)
                    }
                    TextButton(onClick = onClearAll) {
                        Text("清空", color = EvoMindColors.TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatChip(
                    icon = Icons.Default.PersonAdd,
                    label = "新博主",
                    count = newCount,
                    color = EvoMindColors.Success
                )
                StatChip(
                    icon = Icons.Default.Person,
                    label = "已存在",
                    count = existingCount,
                    color = EvoMindColors.TextTertiary
                )
                if (selectedCount > 0) {
                    StatChip(
                        icon = Icons.Default.CheckCircle,
                        label = "已选择",
                        count = selectedCount,
                        color = EvoMindColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BloggerCard(
    blogger: DetectedBlogger,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    val platformColor = when (blogger.platform) {
        "xiaohongshu" -> androidx.compose.ui.graphics.Color(0xFFFF2442)
        "weixin" -> androidx.compose.ui.graphics.Color(0xFF07C160)
        "douyin" -> androidx.compose.ui.graphics.Color(0xFF000000)
        "zhihu" -> androidx.compose.ui.graphics.Color(0xFF0084FF)
        else -> EvoMindColors.Primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (blogger.alreadyExists) 
                EvoMindColors.Surface.copy(alpha = 0.5f) 
            else 
                EvoMindColors.Surface
        ),
        onClick = {
            if (!blogger.alreadyExists) {
                onSelectionChange(!isSelected)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 选择框
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionChange(it) },
                enabled = !blogger.alreadyExists,
                colors = CheckboxDefaults.colors(
                    checkedColor = EvoMindColors.Primary
                )
            )

            // 头像占位
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(platformColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = platformColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = blogger.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (blogger.alreadyExists) 
                        EvoMindColors.TextTertiary 
                    else 
                        EvoMindColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 平台标签
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = platformColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = when (blogger.platform) {
                                "xiaohongshu" -> "小红书"
                                "weixin" -> "微信"
                                "douyin" -> "抖音"
                                "zhihu" -> "知乎"
                                else -> "其他"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = platformColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // 置信度
                    Text(
                        text = "置信度 ${(blogger.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = EvoMindColors.TextTertiary
                    )

                    // 已存在标记
                    if (blogger.alreadyExists) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = EvoMindColors.TextTertiary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "已添加",
                                style = MaterialTheme.typography.labelSmall,
                                color = EvoMindColors.TextTertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportButtonBar(
    selectedCount: Int,
    onImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = EvoMindColors.Background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "已选择 $selectedCount 个",
                style = MaterialTheme.typography.bodyMedium,
                color = EvoMindColors.TextSecondary,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onImport,
                enabled = selectedCount > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EvoMindColors.Primary,
                    disabledContainerColor = EvoMindColors.Primary.copy(alpha = 0.3f)
                )
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("导入信息源")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = EvoMindColors.Error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = EvoMindColors.TextSecondary
            )
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

package com.evomind.ui.components.linkimport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evomind.data.model.LinkImportStatus
import com.evomind.data.model.LinkImportTask
import com.evomind.data.model.PlatformType
import com.evomind.ui.viewmodel.LinkImportViewModel
import java.time.format.DateTimeFormatter

/**
 * 链接导入页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkImportScreen(
    viewModel: LinkImportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // 显示错误提示
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 显示成功提示
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("链接导入") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 链接输入区域
            LinkInputSection(
                url = uiState.url,
                selectedPlatform = uiState.selectedPlatform,
                supportedPlatforms = uiState.supportedPlatforms,
                isLoading = uiState.isLoading,
                onUrlChange = viewModel::onUrlChange,
                onPlatformSelect = viewModel::onPlatformSelect,
                onPaste = {
                    clipboardManager.getText()?.let {
                        viewModel.pasteUrl(it.text)
                    }
                },
                onSubmit = viewModel::submitImport
            )

            // 任务列表
            TaskListSection(
                tasks = uiState.tasks,
                onCancel = viewModel::cancelTask,
                onRetry = viewModel::retryTask,
                onDelete = viewModel::deleteTask
            )
        }
    }
}

/**
 * 链接输入区域
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkInputSection(
    url: String,
    selectedPlatform: PlatformType?,
    supportedPlatforms: List<PlatformType>,
    isLoading: Boolean,
    onUrlChange: (String) -> Unit,
    onPlatformSelect: (PlatformType?) -> Unit,
    onPaste: () -> Unit,
    onSubmit: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "粘贴链接",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 链接输入框
            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("支持小红书、微信公众号、知乎、微博...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    Row {
                        if (url.isNotEmpty()) {
                            IconButton(onClick = { onUrlChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                        IconButton(onClick = onPaste) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "粘贴")
                        }
                    }
                }
            )

            // 平台选择
            Text(
                text = "选择平台（可选）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                supportedPlatforms.forEach { platform ->
                    val isSelected = selectedPlatform == platform
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onPlatformSelect(if (isSelected) null else platform)
                        },
                        label = { Text(platform.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = getPlatformColor(platform)
                                .copy(alpha = 0.2f),
                            selectedLabelColor = getPlatformColor(platform)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            selected = isSelected,
                            enabled = true,
                            borderColor = if (isSelected) getPlatformColor(platform)
                            else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 提交按钮
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = url.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "处理中..." else "开始抓取")
            }
        }
    }
}

/**
 * 任务列表区域
 */
@Composable
private fun TaskListSection(
    tasks: List<LinkImportTask>,
    onCancel: (String) -> Unit,
    onRetry: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Column {
        Text(
            text = "导入任务 (${tasks.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (tasks.isEmpty()) {
            EmptyTaskView()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(tasks.reversed(), key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onCancel = { onCancel(task.id) },
                        onRetry = { onRetry(task.id) },
                        onDelete = { onDelete(task.id) }
                    )
                }
            }
        }
    }
}

/**
 * 空任务视图
 */
@Composable
private fun EmptyTaskView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "暂无导入任务",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "粘贴链接开始抓取内容",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 任务卡片
 */
@Composable
private fun TaskCard(
    task: LinkImportTask,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                LinkImportStatus.SUCCESS -> MaterialTheme.colorScheme.surface
                LinkImportStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 顶部：平台标识和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 平台标签
                val platform = task.platform?.let { PlatformType.fromCode(it) }
                    ?: PlatformType.Unknown
                PlatformChip(platform = platform)

                // 状态指示器
                StatusIndicator(status = task.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 链接
            Text(
                text = task.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 结果展示
            AnimatedVisibility(
                visible = task.result != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                task.result?.let { result ->
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        result.title?.let { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        result.content?.let { content ->
                            Text(
                                text = content.take(100) + if (content.length > 100) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 错误信息
            task.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 时间和操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.createdAt.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when (task.status) {
                        LinkImportStatus.PENDING,
                        LinkImportStatus.SCRAPING,
                        LinkImportStatus.RETRYING -> {
                            IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = "取消",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        LinkImportStatus.FAILED -> {
                            IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "重试",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        else -> {}
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个导入任务吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 平台标签
 */
@Composable
private fun PlatformChip(platform: PlatformType) {
    val color = getPlatformColor(platform)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = platform.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

/**
 * 状态指示器
 */
@Composable
private fun StatusIndicator(status: LinkImportStatus) {
    val (text, color) = when (status) {
        LinkImportStatus.PENDING -> "等待中" to MaterialTheme.colorScheme.onSurfaceVariant
        LinkImportStatus.SCRAPING -> "抓取中" to MaterialTheme.colorScheme.primary
        LinkImportStatus.SUCCESS -> "成功" to MaterialTheme.colorScheme.tertiary
        LinkImportStatus.FAILED -> "失败" to MaterialTheme.colorScheme.error
        LinkImportStatus.RETRYING -> "重试中" to MaterialTheme.colorScheme.secondary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (status == LinkImportStatus.SCRAPING || status == LinkImportStatus.RETRYING) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
                color = color
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

/**
 * 获取平台颜色
 */
private fun getPlatformColor(platform: PlatformType): Color {
    return when (platform) {
        PlatformType.XiaoHongShu -> Color(0xFFFF2442) // 小红书红
        PlatformType.WeChat -> Color(0xFF07C160)      // 微信绿
        PlatformType.Zhihu -> Color(0xFF0084FF)       // 知乎蓝
        PlatformType.Weibo -> Color(0xFFE6162D)       // 微博红
        PlatformType.Unknown -> Color.Gray
    }
}

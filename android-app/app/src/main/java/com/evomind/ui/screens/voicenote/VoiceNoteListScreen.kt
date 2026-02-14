package com.evomind.ui.screens.voicenote

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.evomind.domain.model.VoiceNote
import com.evomind.domain.model.VoiceNote.TranscribeStatus
import java.time.format.DateTimeFormatter

/**
 * 语音笔记列表页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceNoteListScreen(
    viewModel: VoiceNoteViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRecord: () -> Unit,
    onVoiceNoteClick: (VoiceNote) -> Unit
) {
    val voiceNotes by viewModel.voiceNotes.collectAsState()
    val recordingState by viewModel.recordingState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVoiceNotes()
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
                    // 搜索按钮
                    IconButton(onClick = { /* TODO: 打开搜索 */ }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToRecord,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "录音",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        if (voiceNotes.isEmpty()) {
            EmptyVoiceNotesView(
                onRecordClick = onNavigateToRecord,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = voiceNotes,
                    key = { it.id ?: 0 }
                ) { voiceNote ->
                    VoiceNoteItem(
                        voiceNote = voiceNote,
                        onClick = { onVoiceNoteClick(voiceNote) },
                        onFavoriteClick = {
                            viewModel.toggleFavorite(voiceNote.id!!, !voiceNote.isFavorite)
                        },
                        onDeleteClick = {
                            viewModel.deleteVoiceNote(voiceNote.id!!)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 空状态视图
 */
@Composable
private fun EmptyVoiceNotesView(
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MicNone,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "还没有语音笔记",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击下方按钮开始录音",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRecordClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Mic, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("开始录音")
        }
    }
}

/**
 * 语音笔记列表项
 */
@Composable
private fun VoiceNoteItem(
    voiceNote: VoiceNote,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题和收藏按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = voiceNote.getDisplayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (voiceNote.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = if (voiceNote.isFavorite) "取消收藏" else "收藏",
                        tint = if (voiceNote.isFavorite) {
                            Color(0xFFE91E63)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 转写状态
            TranscribeStatusBadge(status = voiceNote.transcribeStatus)

            Spacer(modifier = Modifier.height(8.dp))

            // 转写内容预览
            if (!voiceNote.transcribedText.isNullOrBlank()) {
                Text(
                    text = voiceNote.transcribedText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 底部信息：时长、日期
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = voiceNote.formatDuration(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = voiceNote.recordedAt?.format(
                            DateTimeFormatter.ofPattern("MM-dd HH:mm")
                        ) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 标签
                if (!voiceNote.tags.isNullOrBlank()) {
                    val tagList = voiceNote.getTagsList()
                    if (tagList.isNotEmpty()) {
                        AssistChip(
                            onClick = { },
                            label = { Text(tagList.first()) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 转写状态标签
 */
@Composable
private fun TranscribeStatusBadge(status: TranscribeStatus) {
    val (text, color) = when (status) {
        TranscribeStatus.PENDING -> "待转写" to MaterialTheme.colorScheme.outline
        TranscribeStatus.PROCESSING -> "转写中..." to MaterialTheme.colorScheme.primary
        TranscribeStatus.SUCCESS -> "转写完成" to Color(0xFF4CAF50)
        TranscribeStatus.FAILED -> "转写失败" to MaterialTheme.colorScheme.error
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

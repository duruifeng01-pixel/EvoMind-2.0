package com.evomind.ui.screens.settings

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun GestureSettingsScreen(
    onNavigateBack: () -> Unit
) {
    var swipeEnabled by remember { mutableStateOf(true) }
    var doubleTapEnabled by remember { mutableStateOf(true) }
    var longPressEnabled by remember { mutableStateOf(true) }
    var gestureHaptic by remember { mutableStateOf(true) }
    var lastGesture by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("手势交互") },
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
        ) {
            SettingsItem(
                icon = Icons.Default.Swipe,
                title = "滑动切换",
                subtitle = "左右滑动切换页面",
                checked = swipeEnabled,
                onCheckedChange = { swipeEnabled = it }
            )

            SettingsItem(
                icon = Icons.Default.TouchApp,
                title = "双击操作",
                subtitle = "双击执行快捷操作",
                checked = doubleTapEnabled,
                onCheckedChange = { doubleTapEnabled = it }
            )

            SettingsItem(
                icon = Icons.Default.TouchApp,
                title = "长按操作",
                subtitle = "长按打开更多选项",
                checked = longPressEnabled,
                onCheckedChange = { longPressEnabled = it }
            )

            SettingsItem(
                icon = Icons.Default.Vibration,
                title = "触感反馈",
                subtitle = "手势操作时震动反馈",
                checked = gestureHaptic,
                onCheckedChange = { gestureHaptic = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "手势练习区域",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(200.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { lastGesture = "双击" },
                            onLongPress = { lastGesture = "长按" }
                        )
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            lastGesture = if (dragAmount > 0) "右滑" else "左滑"
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = lastGesture.ifEmpty { "在此区域练习手势" },
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "双击/长按/左右滑动",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

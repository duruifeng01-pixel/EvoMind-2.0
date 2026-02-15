package com.evomind.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ThemeSettingsScreen(
    onNavigateBack: () -> Unit
) {
    var darkMode by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf("medium") }
    var animationEnabled by remember { mutableStateOf(true) }
    var primaryColor by remember { mutableStateOf("blue") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("主题设置") },
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
            SettingsSection(title = "外观") {
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "深色模式",
                    subtitle = "切换深色/浅色主题",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            SettingsSection(title = "字体") {
                SettingsRadioItem(
                    icon = Icons.Default.TextFields,
                    title = "字体大小",
                    options = listOf("small" to "小", "medium" to "中", "large" to "大"),
                    selected = fontSize,
                    onSelect = { fontSize = it }
                )
            }

            SettingsSection(title = "动效") {
                SettingsSwitchItem(
                    icon = Icons.Default.Animation,
                    title = "动画效果",
                    subtitle = "启用页面过渡动画",
                    checked = animationEnabled,
                    onCheckedChange = { animationEnabled = it }
                )
            }

            SettingsSection(title = "主题色") {
                ColorPickerItem(
                    selectedColor = primaryColor,
                    onColorSelect = { primaryColor = it }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
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

@Composable
private fun SettingsRadioItem(
    icon: ImageVector,
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSelect(value) }
                ) {
                    RadioButton(selected = selected == value, onClick = { onSelect(value) })
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun ColorPickerItem(
    selectedColor: String,
    onColorSelect: (String) -> Unit
) {
    val colors = listOf(
        "blue" to "蓝色",
        "cyan" to "青色",
        "green" to "绿色",
        "purple" to "紫色",
        "orange" to "橙色"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "主题色", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.forEach { (value, label) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onColorSelect(value) }
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = when (value) {
                            "blue" -> MaterialTheme.colorScheme.primary
                            "cyan" -> Color(0xFF26C6DA)
                            "green" -> Color(0xFF66BB6A)
                            "purple" -> Color(0xFFAB47BC)
                            "orange" -> Color(0xFFFF7043)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        border = if (selectedColor == value) {
                            ButtonDefaults.outlinedButtonBorder
                        } else null
                    ) {}
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private val Color = androidx.compose.ui.graphics.Color

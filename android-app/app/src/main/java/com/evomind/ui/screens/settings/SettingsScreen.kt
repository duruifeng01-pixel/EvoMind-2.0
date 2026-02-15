package com.evomind.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToUserAgreement: () -> Unit,
    onNavigateToAigcCompliance: () -> Unit,
    onNavigateToDataExport: () -> Unit,
    onNavigateToAccountDeletion: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 账户设置
            SettingsSectionTitle("账户与安全")

            SettingsItem(
                icon = Icons.Default.Download,
                title = "数据导出",
                subtitle = "导出您的个人数据",
                onClick = onNavigateToDataExport
            )

            SettingsItem(
                icon = Icons.Default.DeleteForever,
                title = "注销账号",
                subtitle = "永久删除您的账号",
                onClick = onNavigateToAccountDeletion,
                isDestructive = true
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // 合规与隐私
            SettingsSectionTitle("合规与隐私")

            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "隐私政策",
                subtitle = "了解我们如何保护您的隐私",
                onClick = onNavigateToPrivacyPolicy
            )

            SettingsItem(
                icon = Icons.Default.Description,
                title = "用户协议",
                subtitle = "查看用户服务协议",
                onClick = onNavigateToUserAgreement
            )

            SettingsItem(
                icon = Icons.Default.SmartToy,
                title = "AIGC合规说明",
                subtitle = "了解AI生成内容的规范",
                onClick = onNavigateToAigcCompliance
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // 关于
            SettingsSectionTitle("关于")

            SettingsItem(
                icon = Icons.Default.Info,
                title = "关于 EvoMind",
                subtitle = "版本 1.0.0",
                onClick = { }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 退出登录
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("退出登录")
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error 
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

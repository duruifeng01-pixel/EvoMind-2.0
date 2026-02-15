package com.evomind.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 首次启动弹窗
 * 展示隐私政策、用户协议和AIGC说明
 */
@Composable
fun FirstLaunchDialog(
    onAgree: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToUserAgreement: () -> Unit,
    onNavigateToAigcCompliance: () -> Unit
) {
    var agreed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "欢迎使用 EvoMind",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "在开始使用之前，请您阅读并同意以下协议：",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 协议链接列表
                AgreementItem(
                    title = "《隐私政策》",
                    description = "了解我们如何收集、使用和保护您的个人信息",
                    onClick = onNavigateToPrivacyPolicy
                )

                AgreementItem(
                    title = "《用户协议》",
                    description = "了解您使用我们服务的权利和义务",
                    onClick = onNavigateToUserAgreement
                )

                AgreementItem(
                    title = "《AIGC合规说明》",
                    description = "了解AI生成内容的特性和使用规范",
                    onClick = onNavigateToAigcCompliance
                )

                Spacer(modifier = Modifier.height(16.dp))

                // AIGC特别提示
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "AI生成内容提示",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "本应用使用人工智能技术为您提供服务。" +
                                   "AI生成的内容可能存在错误，仅供参考，不构成专业建议。",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 同意复选框
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = agreed,
                        onCheckedChange = { agreed = it }
                    )
                    Text(
                        text = "我已阅读并同意《隐私政策》和《用户协议》",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAgree,
                enabled = agreed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("同意并继续")
            }
        },
        dismissButton = null
    )
}

@Composable
private fun AgreementItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 简化的首次启动页面（全屏版本）
 */
@Composable
fun FirstLaunchScreen(
    onAgree: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToUserAgreement: () -> Unit,
    onNavigateToAigcCompliance: () -> Unit
) {
    var agreed by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo
            Text(
                text = "EvoMind",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "个人成长认知外骨骼",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "欢迎使用",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "在开始使用之前，请您阅读并同意以下协议",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            // 协议链接
            AgreementItem(
                title = "《隐私政策》",
                description = "了解我们如何收集、使用和保护您的个人信息",
                onClick = onNavigateToPrivacyPolicy
            )

            AgreementItem(
                title = "《用户协议》",
                description = "了解您使用我们服务的权利和义务",
                onClick = onNavigateToUserAgreement
            )

            AgreementItem(
                title = "《AIGC合规说明》",
                description = "了解AI生成内容的特性和使用规范",
                onClick = onNavigateToAigcCompliance
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AIGC提示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ AI生成内容提示",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "本应用使用人工智能技术为您提供服务。" +
                               "AI生成的内容可能存在错误，仅供参考，不构成专业建议。" +
                               "涉及重要决策时，请咨询相关领域专业人士。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 同意复选框
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it }
                )
                Text(
                    text = "我已阅读并同意《隐私政策》和《用户协议》",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 同意按钮
            Button(
                onClick = onAgree,
                enabled = agreed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("同意并进入应用")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

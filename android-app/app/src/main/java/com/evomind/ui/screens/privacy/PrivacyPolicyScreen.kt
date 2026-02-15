package com.evomind.ui.screens.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 隐私政策页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("隐私政策") },
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
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "EvoMind 隐私政策",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "最后更新日期：2026年2月15日",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            PolicySection("1. 引言") {
                PolicyParagraph(
                    "EvoMind（以下简称"我们"、"本应用"）深知个人信息对您的重要性，" +
                    "我们会尽力保护您的个人信息安全。我们致力于维持您对我们的信任，" +
                    "恪守以下原则保护您的个人信息：权责一致原则、目的明确原则、" +
                    "选择同意原则、最小必要原则、确保安全原则、主体参与原则、" +
                    "公开透明原则等。"
                )
                PolicyParagraph(
                    "本隐私政策将帮助您了解以下内容：我们如何收集和使用您的个人信息、" +
                    "我们如何共享、转让、公开披露您的个人信息、我们如何保护和存储您的个人信息、" +
                    "您如何管理您的个人信息等。"
                )
            }

            PolicySection("2. 我们收集的信息") {
                PolicyParagraph("我们仅在必要的情况下收集以下类型的信息：")
                PolicyBullet("账户信息：手机号、昵称、头像（用于创建和管理您的账户）")
                PolicyBullet("设备信息：设备型号、操作系统版本、唯一设备标识符（用于优化应用性能）")
                PolicyBullet("使用数据：阅读时长、收藏记录、学习进度（用于个性化推荐）")
                PolicyBullet("内容数据：您导入的信息源、收藏的认知卡片、语音笔记（仅在您主动操作时收集）")
            }

            PolicySection("3. 本地优先存储") {
                PolicyParagraph(
                    "EvoMind采用"本地优先"架构设计。您的核心数据（认知卡片、语料库、" +
                    "学习记录等）主要存储在您的设备本地，而非云端服务器。这意味着："
                )
                PolicyBullet("您的数据所有权完全归属于您")
                PolicyBullet("即使在没有网络连接的情况下，您也可以正常使用大部分功能")
                PolicyBullet("数据同步仅在您明确授权时进行")
            }

            PolicySection("4. 数据使用目的") {
                PolicyParagraph("我们使用您的数据用于以下目的：")
                PolicyBullet("提供、维护和改进我们的服务")
                PolicyBullet("个性化您的学习体验和内容推荐")
                PolicyBullet("生成您的认知画像和学习分析报告")
                PolicyBullet("保护账户安全，防止欺诈行为")
                PolicyBullet("遵守法律法规要求")
            }

            PolicySection("5. 数据共享与披露") {
                PolicyParagraph(
                    "我们承诺不会出售您的个人信息。仅在以下情况下可能会共享您的信息："
                )
                PolicyBullet("获得您的明确同意")
                PolicyBullet("与授权服务提供商共享（如云存储、AI服务提供商，且仅用于提供服务）")
                PolicyBullet("遵守法律法规或响应法律程序")
                PolicyBullet("保护我们或他人的权利、财产或安全")
            }

            PolicySection("6. 数据安全") {
                PolicyParagraph(
                    "我们采用业界标准的安全措施保护您的数据："
                )
                PolicyBullet("数据传输使用TLS加密")
                PolicyBullet("本地数据使用SQLCipher加密存储")
                PolicyBullet("实施严格的访问控制")
                PolicyBullet("定期进行安全审计")
            }

            PolicySection("7. 您的权利") {
                PolicyParagraph("根据适用的数据保护法律，您拥有以下权利：")
                PolicyBullet("访问权：获取我们持有的关于您的个人信息的副本")
                PolicyBullet("更正权：更正不准确的个人信息")
                PolicyBullet("删除权：要求删除您的个人信息（账户注销）")
                PolicyBullet("数据可携带权：导出您的数据")
                PolicyBullet("撤回同意权：撤回之前给予的同意")
            }

            PolicySection("8. 未成年人保护") {
                PolicyParagraph(
                    "我们的服务不面向14岁以下的未成年人。如果我们发现收集了未成年人的个人信息，" +
                    "将会尽快删除相关数据。"
                )
            }

            PolicySection("9. 政策更新") {
                PolicyParagraph(
                    "我们可能会不时更新本隐私政策。更新后的政策将在应用内公布，" +
                    "重大变更我们会通过弹窗或推送通知您。"
                )
            }

            PolicySection("10. 联系我们") {
                PolicyParagraph(
                    "如果您对本隐私政策有任何疑问或建议，请通过以下方式联系我们："
                )
                PolicyBullet("邮箱：privacy@evomind.app")
                PolicyBullet("地址：北京市海淀区XXX路XXX号")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun PolicyParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun PolicyBullet(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

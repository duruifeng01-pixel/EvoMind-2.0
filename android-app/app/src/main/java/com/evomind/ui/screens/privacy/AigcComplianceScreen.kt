package com.evomind.ui.screens.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * AIGC合规说明页面
 * 说明AI生成内容的特性、局限性和使用规范
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AigcComplianceScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AIGC合规说明") },
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
            // 顶部警告卡片
            AigcWarningCard()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AI生成内容说明",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "最后更新日期：2026年2月15日",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            PolicySection("1. 什么是AI生成内容") {
                PolicyParagraph(
                    "EvoMind使用人工智能技术（AI）为您提供内容生成服务。" +
                    "当您使用以下功能时，系统会自动生成内容："
                )
                PolicyBullet("认知卡片：AI自动提取文章核心观点、生成摘要和脑图")
                PolicyBullet("苏格拉底对话：AI根据您的话题生成追问和引导")
                PolicyBullet("冲突检测：AI分析内容与您认知体系的差异")
                PolicyBullet("进化计划：AI根据您的学习诉求生成个性化计划")
            }

            PolicySection("2. AI生成内容的特性") {
                PolicyParagraph("请您了解AI生成内容的以下特性：")
                
                AigcFeatureCard(
                    title = "非完美准确",
                    description = "AI可能产生错误、偏见或不完整的信息，" +
                            "请务必对重要信息进行核实。"
                )
                
                AigcFeatureCard(
                    title = "基于训练数据",
                    description = "AI的输出基于其训练数据，可能无法涵盖最新信息或特定领域的专业知识。"
                )
                
                AigcFeatureCard(
                    title = "概率性生成",
                    description = "相同输入可能产生不同输出，AI不保证结果的一致性。"
                )
                
                AigcFeatureCard(
                    title = "无真实理解",
                    description = "AI并不真正"理解"内容，只是基于模式匹配生成文本。"
                )
            }

            PolicySection("3. 使用规范") {
                PolicyParagraph("使用AI生成功能时，请遵守以下规范：")
                PolicyBullet("不得利用AI生成违法、违规或有害内容")
                PolicyBullet("不得使用AI生成的内容进行学术造假或欺诈")
                PolicyBullet("涉及专业决策（医疗、法律、金融等）时，请咨询专业人士")
                PolicyBullet("尊重知识产权，不得使用AI大量复制他人作品")
            }

            PolicySection("4. 内容审核机制") {
                PolicyParagraph(
                    "为确保内容合规，我们建立了多重审核机制："
                )
                PolicyBullet("预过滤：对用户输入进行敏感词和违规内容检测")
                PolicyBullet("生成后审核：对AI生成内容进行合规性检查")
                PolicyBullet("用户反馈：用户可以举报不当内容")
                PolicyBullet("人工复核：对疑似违规内容进行人工审核")
            }

            PolicySection("5. 标注要求") {
                PolicyParagraph(
                    "根据国家互联网信息办公室《生成式人工智能服务管理暂行办法》，" +
                    "所有AI生成内容必须明确标注。EvoMind采用以下标注方式："
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI生成，仅供参考",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                PolicyParagraph(
                    "此标注会出现在所有AI生成内容的显著位置，您不得删除或篡改该标注。"
                )
            }

            PolicySection("6. 隐私与数据安全") {
                PolicyParagraph(
                    "使用AI服务时，您的数据处理方式如下："
                )
                PolicyBullet("内容分析：您输入的文本会发送至AI服务进行分析")
                PolicyBullet("数据加密：传输过程使用TLS加密，保护数据安全")
                PolicyBullet("数据留存：AI服务提供商可能会临时存储数据以提供服务，" +
                        "但不会用于训练其模型或与他方共享")
                PolicyBullet("本地优先：您的核心数据仍然主要存储在本地设备")
            }

            PolicySection("7. 知识产权") {
                PolicyParagraph(
                    "关于AI生成内容的知识产权归属："
                )
                PolicyBullet("您拥有AI生成内容的使用权，可用于个人学习、研究等目的")
                PolicyBullet("AI生成内容可能包含训练数据中的元素，您应确保使用不侵犯他人权利")
                PolicyBullet("商业使用AI生成内容时，请注意相关法律风险")
            }

            PolicySection("8. 免责声明") {
                PolicyParagraph(
                    "8.1 我们不对AI生成内容的准确性、完整性或适用性作任何保证。"
                )
                PolicyParagraph(
                    "8.2 因使用AI生成内容而产生的任何后果，由用户自行承担。"
                )
                PolicyParagraph(
                    "8.3 如AI生成内容存在违规情况，请立即停止使用并举报，" +
                    "我们将及时处理。"
                )
            }

            PolicySection("9. 联系方式") {
                PolicyParagraph(
                    "如您对AI生成内容有任何疑问、建议或投诉，请联系我们："
                )
                PolicyBullet("邮箱：aigc-compliance@evomind.app")
                PolicyBullet("应用内：设置 → 帮助与反馈 → AIGC问题反馈")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AigcWarningCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "重要提示",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "AI生成的内容可能存在错误，请务必核实重要信息。" +
                           "涉及专业决策时，请咨询相关领域专业人士。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun AigcFeatureCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

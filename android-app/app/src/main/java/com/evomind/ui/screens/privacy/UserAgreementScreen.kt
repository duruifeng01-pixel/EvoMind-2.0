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
 * 用户协议页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAgreementScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户协议") },
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
                text = "EvoMind 用户协议",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "最后更新日期：2026年2月15日",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            PolicySection("1. 协议接受") {
                PolicyParagraph(
                    "欢迎使用EvoMind（以下简称"本应用"）。本用户协议（以下简称"本协议"）" +
                    "是您（以下简称"用户"或"您"）与本应用运营方（以下简称"我们"）之间" +
                    "关于使用本应用服务的协议。"
                )
                PolicyParagraph(
                    "请您仔细阅读并充分理解本协议的全部内容。如您不同意本协议的任何内容，" +
                    "请不要注册或使用本应用服务。您点击"同意"或实际使用本应用服务，" +
                    "即视为您已阅读并同意本协议的全部内容。"
                )
            }

            PolicySection("2. 服务说明") {
                PolicyParagraph(
                    "EvoMind是一款基于人工智能技术的个人成长认知外骨骼应用，" +
                    "旨在帮助用户构建个人知识体系、提升认知能力。主要功能包括："
                )
                PolicyBullet("信息源管理：导入和管理个人关注的信息源")
                PolicyBullet("认知卡片：AI辅助生成和管理知识卡片")
                PolicyBullet("7:3信息流：智能推荐个性化内容")
                PolicyBullet("苏格拉底式对话：AI引导深度思考")
                PolicyBullet("本地语料库：个人知识沉淀和管理")
                PolicyBullet("语音笔记：语音记录和转写")
            }

            PolicySection("3. 账号注册与安全") {
                PolicyParagraph(
                    "3.1 您承诺以真实、准确、完整的信息注册账号，并在信息变更时及时更新。"
                )
                PolicyParagraph(
                    "3.2 您应妥善保管账号密码，对账号下的所有行为承担法律责任。" +
                    "如发现账号被盗或存在安全问题，请立即联系我们。"
                )
                PolicyParagraph(
                    "3.3 我们有权基于合理怀疑暂停或终止涉嫌违规的账号。"
                )
            }

            PolicySection("4. 用户行为规范") {
                PolicyParagraph("您在使用本应用服务时应遵守以下规范：")
                PolicyBullet("遵守中华人民共和国法律法规")
                PolicyBullet("不得发布、传播违法、违规或有害信息")
                PolicyBullet("不得侵犯他人知识产权、隐私权等合法权益")
                PolicyBullet("不得利用本应用从事任何商业牟利活动")
                PolicyBullet("不得对本应用进行反向工程、破解或攻击")
                PolicyBullet("不得滥用AI生成功能，包括但不限于生成违法内容")
            }

            PolicySection("5. AI生成内容声明") {
                PolicyParagraph(
                    "5.1 本应用使用人工智能技术（包括但不限于DeepSeek等模型）" +
                    "为用户提供内容生成服务，包括但不限于：认知卡片生成、脑图生成、" +
                    "苏格拉底式对话等。"
                )
                PolicyParagraph(
                    "5.2 AI生成的内容仅供参考，不构成专业建议。对于重要决策，" +
                    "建议您咨询相关领域专业人士。"
                )
                PolicyParagraph(
                    "5.3 我们不保证AI生成内容的准确性、完整性或适用性，" +
                    "您应自行判断并承担使用风险。"
                )
                PolicyParagraph(
                    "5.4 所有AI生成内容均会标注"AI生成，仅供参考"，您不得删除或篡改该标注。"
                )
            }

            PolicySection("6. 知识产权") {
                PolicyParagraph(
                    "6.1 本应用的界面设计、代码、商标、标识等知识产权归我们所有。" +
                    "未经授权，您不得复制、修改、传播或用于商业用途。"
                )
                PolicyParagraph(
                    "6.2 您在使用本应用过程中生成的内容（如认知卡片、语音笔记、语料库等）" +
                    "的知识产权归您所有。"
                )
                PolicyParagraph(
                    "6.3 您授予我们非独家的、免费的许可，以便我们为您提供服务" +
                    "（如数据同步、AI处理等）。"
                )
            }

            PolicySection("7. 隐私保护") {
                PolicyParagraph(
                    "我们重视您的隐私保护，具体请参考我们的《隐私政策》。" +
                    "使用本应用服务即表示您同意我们按照隐私政策收集、使用和保护您的信息。"
                )
            }

            PolicySection("8. 服务变更与终止") {
                PolicyParagraph(
                    "8.1 我们有权根据业务发展需要修改、中断或终止部分或全部服务，" +
                    "并会提前通知您。"
                )
                PolicyParagraph(
                    "8.2 如您违反本协议，我们有权暂停或终止向您提供服务。"
                )
                PolicyParagraph(
                    "8.3 服务终止后，您本地存储的数据不受影响，但云端同步功能将停止。"
                )
            }

            PolicySection("9. 免责声明") {
                PolicyParagraph(
                    "9.1 本应用按"现状"和"可用性"提供，我们不作任何明示或暗示的担保。"
                )
                PolicyParagraph(
                    "9.2 对于因不可抗力、系统维护、第三方原因等导致的服务中断，" +
                    "我们不承担责任。"
                )
                PolicyParagraph(
                    "9.3 对于您因使用或无法使用本应用而产生的任何损失，" +
                    "在法律允许的最大范围内，我们不承担责任。"
                )
            }

            PolicySection("10. 争议解决") {
                PolicyParagraph(
                    "10.1 本协议的订立、执行和解释均适用中华人民共和国法律。"
                )
                PolicyParagraph(
                    "10.2 因本协议引起的或与本协议有关的任何争议，" +
                    "双方应友好协商解决；协商不成的，任何一方均可向" +
                    "北京市海淀区人民法院提起诉讼。"
                )
            }

            PolicySection("11. 协议修改") {
                PolicyParagraph(
                    "我们有权不时修改本协议。修改后的协议将在应用内公布，" +
                    "重大变更我们会通过弹窗或推送通知您。您继续使用本应用服务，" +
                    "即视为接受修改后的协议。"
                )
            }

            PolicySection("12. 其他") {
                PolicyParagraph(
                    "12.1 本协议的任何条款被认定为无效或不可执行，不影响其他条款的效力。"
                )
                PolicyParagraph(
                    "12.2 本协议构成双方就本协议主题达成的完整协议，" +
                    "取代之前的所有口头或书面协议。"
                )
                PolicyParagraph(
                    "12.3 如您对本协议有任何疑问，请联系我们：support@evomind.app"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

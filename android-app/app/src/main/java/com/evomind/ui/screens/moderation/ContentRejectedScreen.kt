package com.evomind.ui.screens.moderation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evomind.ui.theme.EvoMindTheme

/**
 * 内容审核不通过提示页面
 * 
 * 当用户发布的内容或AI生成的内容未通过审核时显示
 * 解释违规原因并提供申诉/修改建议
 */

/**
 * 违规类型
 */
enum class ViolationType {
    POLITICS,           // 政治敏感
    PORNOGRAPHY,        // 色情
    VIOLENCE,           // 暴力
    TERRORISM,          // 恐怖主义
    GAMBLING,           // 赌博
    FRAUD,              // 诈骗
    ABUSE,              // 辱骂
    ADVERTISEMENT,      // 广告违规
    SENSITIVE_WORD,     // 敏感词
    OTHER               // 其他
}

/**
 * 审核结果数据
 */
data class ModerationResult(
    val status: ModerationStatus,
    val violationType: ViolationType? = null,
    val violationDetails: String? = null,
    val hitWords: List<String> = emptyList(),
    val suggestion: String? = null
)

enum class ModerationStatus {
    APPROVED,
    REJECTED,
    NEED_REVIEW,
    ERROR
}

/**
 * 审核不通过页面
 * 
 * @param violationType 违规类型
 * @param violationDetails 违规详情
 * @param hitWords 命中敏感词列表
 * @param contentPreview 内容预览
 * @param onBack 返回回调
 * @param onEdit 编辑内容回调
 * @param onAppeal 申诉回调
 * @param onContactSupport 联系客服回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentRejectedScreen(
    violationType: ViolationType,
    violationDetails: String? = null,
    hitWords: List<String> = emptyList(),
    contentPreview: String? = null,
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onAppeal: () -> Unit = {},
    onContactSupport: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("审核结果") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 违规图标和标题
            RejectedHeader(violationType)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 违规详情卡片
            ViolationDetailsCard(
                violationType = violationType,
                violationDetails = violationDetails,
                hitWords = hitWords
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 内容预览（如果提供）
            contentPreview?.let {
                ContentPreviewCard(contentPreview = it)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 修改建议
            SuggestionCard(violationType = violationType)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 操作按钮
            ActionButtons(
                onEdit = onEdit,
                onAppeal = onAppeal
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 帮助链接
            TextButton(
                onClick = onContactSupport,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("联系客服获取帮助")
            }
        }
    }
}

/**
 * 审核不通过头部
 */
@Composable
private fun RejectedHeader(violationType: ViolationType) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 大图标
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 标题
        Text(
            text = "审核未通过",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 副标题
        Text(
            text = "您发布的内容未通过审核",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 违规类型标签
        ViolationTypeBadge(violationType = violationType)
    }
}

/**
 * 违规类型标签
 */
@Composable
private fun ViolationTypeBadge(violationType: ViolationType) {
    val (icon, text, color) = when (violationType) {
        ViolationType.POLITICS -> Triple(Icons.Default.Gavel, "政治敏感", Color(0xFFB71C1C))
        ViolationType.PORNOGRAPHY -> Triple(Icons.Default.Warning, "色情低俗", Color(0xFF880E4F))
        ViolationType.VIOLENCE -> Triple(Icons.Default.ReportProblem, "暴力血腥", Color(0xFFE65100))
        ViolationType.TERRORISM -> Triple(Icons.Default.Flag, "恐怖主义", Color(0xFFBF360C))
        ViolationType.GAMBLING -> Triple(Icons.Default.Block, "赌博", Color(0xFF4A148C))
        ViolationType.FRAUD -> Triple(Icons.Default.ErrorOutline, "诈骗欺诈", Color(0xFF00695C))
        ViolationType.ABUSE -> Triple(Icons.Default.ContentCut, "辱骂攻击", Color(0xFF1565C0))
        ViolationType.ADVERTISEMENT -> Triple(Icons.Default.Info, "广告违规", Color(0xFF546E7A))
        ViolationType.SENSITIVE_WORD -> Triple(Icons.Default.Warning, "敏感词", Color(0xFFEF6C00))
        ViolationType.OTHER -> Triple(Icons.Default.HelpOutline, "其他违规", Color(0xFF757575))
    }
    
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = color
        )
    }
}

/**
 * 违规详情卡片
 */
@Composable
private fun ViolationDetailsCard(
    violationType: ViolationType,
    violationDetails: String?,
    hitWords: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "违规详情",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 具体原因
            violationDetails?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } ?: Text(
                text = getDefaultViolationDescription(violationType),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 命中敏感词
            if (hitWords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "检测到以下敏感内容:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hitWords.take(5).forEach { word ->
                        HitWordChip(word = word)
                    }
                }
            }
        }
    }
}

/**
 * 命中词标签
 */
@Composable
private fun HitWordChip(word: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 内容预览卡片
 */
@Composable
private fun ContentPreviewCard(contentPreview: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "内容预览",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (contentPreview.length > 200) 
                    contentPreview.take(200) + "..." 
                else 
                    contentPreview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 修改建议卡片
 */
@Composable
private fun SuggestionCard(violationType: ViolationType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "修改建议",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = getModificationSuggestion(violationType),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 操作按钮
 */
@Composable
private fun ActionButtons(
    onEdit: () -> Unit,
    onAppeal: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 编辑按钮
        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "修改内容",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                    )
                )
            }

        // 申诉按钮
        OutlinedButton(
            onClick = onAppeal,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "申诉审核",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * 获取默认违规描述
 */
private fun getDefaultViolationDescription(violationType: ViolationType): String {
    return when (violationType) {
        ViolationType.POLITICS -> "您的内容涉及政治敏感话题，违反社区规范。"
        ViolationType.PORNOGRAPHY -> "您的内容包含色情低俗信息，违反社区规范。"
        ViolationType.VIOLENCE -> "您的内容包含暴力血腥信息，违反社区规范。"
        ViolationType.TERRORISM -> "您的内容涉及恐怖主义或极端主义，违反法律法规。"
        ViolationType.GAMBLING -> "您的内容涉及赌博或博彩信息，违反社区规范。"
        ViolationType.FRAUD -> "您的内容涉及诈骗或欺诈信息，违反社区规范。"
        ViolationType.ABUSE -> "您的内容包含辱骂或攻击性语言，违反社区规范。"
        ViolationType.ADVERTISEMENT -> "您的内容包含违规广告或垃圾信息，违反社区规范。"
        ViolationType.SENSITIVE_WORD -> "您的内容包含敏感词汇，请修改后重新提交。"
        ViolationType.OTHER -> "您的内容违反社区规范，请修改后重新提交。"
    }
}

/**
 * 获取修改建议
 */
private fun getModificationSuggestion(violationType: ViolationType): String {
    return when (violationType) {
        ViolationType.POLITICS -> "请避免讨论敏感政治话题，专注于个人成长和学习内容。"
        ViolationType.PORNOGRAPHY -> "请移除所有色情低俗内容，保持内容健康积极。"
        ViolationType.VIOLENCE -> "请避免描述暴力血腥场景，维护良好的社区环境。"
        ViolationType.TERRORISM -> "请立即删除所有涉及恐怖主义的内容，此类内容将永久封禁。"
        ViolationType.GAMBLING -> "请移除赌博相关内容，社区禁止任何形式的赌博推广。"
        ViolationType.FRAUD -> "请确保内容真实可信，删除任何可能误导他人的信息。"
        ViolationType.ABUSE -> "请使用文明语言，尊重他人，避免攻击性言论。"
        ViolationType.ADVERTISEMENT -> "请避免发布广告或推广内容，如需推广请联系商务合作。"
        ViolationType.SENSITIVE_WORD -> "请检查并替换敏感词汇，使用更中性的表达方式。"
        ViolationType.OTHER -> "请仔细阅读社区规范，修改不符合要求的内容后重新提交。"
    }
}

// ==================== 预览 ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ContentRejectedScreenPreview() {
    EvoMindTheme {
        ContentRejectedScreen(
            violationType = ViolationType.SENSITIVE_WORD,
            violationDetails = "检测到内容包含敏感词汇，请修改后重新提交。",
            hitWords = listOf("敏感词1", "敏感词2", "敏感词3"),
            contentPreview = "这是一段包含敏感词的内容示例，用于展示审核不通过页面。在实际应用中，这里会显示用户提交的原始内容。"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ViolationTypeBadgePreview() {
    EvoMindTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ViolationType.values().forEach { type ->
                ViolationTypeBadge(violationType = type)
            }
        }
    }
}
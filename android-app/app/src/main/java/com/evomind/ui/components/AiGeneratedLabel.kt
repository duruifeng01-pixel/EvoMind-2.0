package com.evomind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evomind.ui.theme.EvoMindTheme

/**
 * AI生成内容标注组件
 * 
 * 根据国内AIGC合规要求，所有AI生成的内容必须明确标注
 * 参考：《生成式人工智能服务管理暂行办法》
 */

/**
 * AI生成标签样式
 */
enum class AiLabelStyle {
    SMALL,      // 小型标签（用于卡片角落）
    MEDIUM,     // 中型标签（用于内容头部）
    LARGE,      // 大型标签（用于独立说明）
    INLINE      // 行内标签（用于文本中）
}

/**
 * AI生成标签位置
 */
enum class AiLabelPosition {
    START,      // 内容开头
    END,        // 内容结尾
    TOP_RIGHT,  // 右上角
    BOTTOM_LEFT // 左下角
}

/**
 * 标准AI生成标签
 * 
 * @param style 标签样式
 * @param showIcon 是否显示图标
 * @param customText 自定义文本（默认"AI生成"）
 * @param modifier 修饰符
 */
@Composable
fun AiGeneratedLabel(
    style: AiLabelStyle = AiLabelStyle.MEDIUM,
    showIcon: Boolean = true,
    customText: String? = null,
    modifier: Modifier = Modifier
) {
    val text = customText ?: "AI生成"
    
    when (style) {
        AiLabelStyle.SMALL -> SmallAiLabel(text, showIcon, modifier)
        AiLabelStyle.MEDIUM -> MediumAiLabel(text, showIcon, modifier)
        AiLabelStyle.LARGE -> LargeAiLabel(text, showIcon, modifier)
        AiLabelStyle.INLINE -> InlineAiLabel(text, showIcon, modifier)
    }
}

/**
 * 小型AI标签
 * 适用于：卡片角落、列表项
 */
@Composable
private fun SmallAiLabel(
    text: String,
    showIcon: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showIcon) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 中型AI标签
 * 适用于：内容头部、段落标题
 */
@Composable
private fun MediumAiLabel(
    text: String,
    showIcon: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showIcon) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 大型AI标签
 * 适用于：独立说明、弹窗提示
 */
@Composable
private fun LargeAiLabel(
    text: String,
    showIcon: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showIcon) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 行内AI标签
 * 适用于：文本段落中
 */
@Composable
private fun InlineAiLabel(
    text: String,
    showIcon: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIcon) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
        Text(
            text = "[$text]",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * AI生成内容说明卡片
 * 包含完整的AI生成说明和免责声明
 */
@Composable
fun AiGeneratedDisclaimerCard(
    modifier: Modifier = Modifier,
    aiModel: String? = null,
    generatedAt: String? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            androidx.compose.foundation.layout.Column {
                Text(
                    text = "AI生成内容",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = buildString {
                        append("以上内容由人工智能生成，仅供参考。")
                        append("AI输出可能存在不准确之处，请结合实际情况自行判断。")
                        aiModel?.let { append("\n生成模型: $it") }
                        generatedAt?.let { append("\n生成时间: $it") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * AI生成内容包装器
 * 自动在内容周围添加AI标注
 */
@Composable
fun AiGeneratedContentWrapper(
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    showDisclaimer: Boolean = true,
    labelPosition: AiLabelPosition = AiLabelPosition.START,
    aiModel: String? = null,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        // 顶部标签
        if (showLabel && labelPosition == AiLabelPosition.START) {
            AiGeneratedLabel(style = AiLabelStyle.MEDIUM)
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        // 内容
        content()
        
        // 底部免责声明
        if (showDisclaimer) {
            Spacer(modifier = Modifier.padding(vertical = 12.dp))
            AiGeneratedDisclaimerCard(aiModel = aiModel)
        }
        
        // 尾部标签
        if (showLabel && labelPosition == AiLabelPosition.END) {
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                AiGeneratedLabel(style = AiLabelStyle.MEDIUM)
            }
        }
    }
}

/**
 * 认知卡片的AI标注
 * 专门用于Card组件的AI标注
 */
@Composable
fun CardAiGeneratedBadge(
    modifier: Modifier = Modifier,
    showDisclaimer: Boolean = false,
    aiModel: String? = null
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        AiGeneratedLabel(style = AiLabelStyle.SMALL)
        
        if (showDisclaimer) {
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "AI生成内容，仅供参考",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== 预览 ====================

@Preview(showBackground = true)
@Composable
private fun AiGeneratedLabelPreview() {
    EvoMindTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AiGeneratedLabel(style = AiLabelStyle.SMALL)
            AiGeneratedLabel(style = AiLabelStyle.MEDIUM)
            AiGeneratedLabel(style = AiLabelStyle.LARGE)
            AiGeneratedLabel(style = AiLabelStyle.INLINE)
            AiGeneratedLabel(style = AiLabelStyle.MEDIUM, customText = "AI总结")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AiGeneratedDisclaimerCardPreview() {
    EvoMindTheme {
        AiGeneratedDisclaimerCard(
            modifier = Modifier.padding(16.dp),
            aiModel = "DeepSeek-V3",
            generatedAt = "2026-02-15 10:30"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AiGeneratedContentWrapperPreview() {
    EvoMindTheme {
        AiGeneratedContentWrapper(
            modifier = Modifier.padding(16.dp),
            aiModel = "DeepSeek-V3"
        ) {
            Text(
                text = "这是一段由AI生成的内容示例。AI可以帮助我们快速生成摘要、洞察和脑图。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
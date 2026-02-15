package com.evomind.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * 算力成本统计页面
 * 展示用户的算力成本明细和订阅费预估
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComputingCostScreen(
    onNavigateBack: () -> Unit,
    viewModel: ComputingCostViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("算力成本") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 订阅费预估卡片
            item {
                SubscriptionEstimateCard(
                    monthlyEstimate = uiState.monthlyEstimate,
                    costMultiplier = uiState.costMultiplier
                )
            }

            // 成本计算公式说明
            item {
                FormulaCard(formula = uiState.formulaDescription)
            }

            // 成本明细
            item {
                CostBreakdownCard(
                    totalCost = uiState.totalCost,
                    ocrCost = uiState.ocrCost,
                    aiCost = uiState.aiCost,
                    crawlCost = uiState.crawlCost,
                    storageCost = uiState.storageCost
                )
            }

            // 使用统计
            item {
                UsageStatsCard(
                    sourceCount = uiState.sourceCount,
                    ocrCount = uiState.ocrRequestCount,
                    aiTokenCount = uiState.aiTokenCount,
                    dialogueCount = uiState.dialogueTurnCount,
                    conflictCount = uiState.conflictMarkCount
                )
            }

            // 单价说明
            item {
                UnitPriceCard(unitPrices = uiState.unitPrices)
            }

            // 透明定价说明
            item {
                TransparentPricingCard()
            }
        }
    }
}

@Composable
private fun SubscriptionEstimateCard(
    monthlyEstimate: BigDecimal,
    costMultiplier: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "预估月订阅费",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¥${monthlyEstimate.format(2)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "基于透明成本 × $costMultiplier 倍定价",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FormulaCard(formula: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "计算公式",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formula,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CostBreakdownCard(
    totalCost: BigDecimal,
    ocrCost: BigDecimal,
    aiCost: BigDecimal,
    crawlCost: BigDecimal,
    storageCost: BigDecimal
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "成本明细（30天）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            CostItem("总成本", totalCost, isTotal = true)
            CostItem("OCR识别", ocrCost)
            CostItem("AI调用", aiCost)
            CostItem("内容抓取", crawlCost)
            CostItem("存储", storageCost)
        }
    }
}

@Composable
private fun CostItem(label: String, amount: BigDecimal, isTotal: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Medium else FontWeight.Normal
        )
        Text(
            text = "¥${amount.format(if (isTotal) 2 else 4)}",
            style = if (isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Medium else FontWeight.Normal,
            color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UsageStatsCard(
    sourceCount: Int,
    ocrCount: Int,
    aiTokenCount: Long,
    dialogueCount: Int,
    conflictCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "使用统计（30天）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem(
                    label = "信息源",
                    value = "$sourceCount",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "OCR识别",
                    value = "$ocrCount",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "AI Token",
                    value = formatNumber(aiTokenCount),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem(
                    label = "对话轮次",
                    value = "$dialogueCount",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "冲突标记",
                    value = "$conflictCount",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "",
                    value = "",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UnitPriceCard(unitPrices: List<UnitPriceItem>) {
    if (unitPrices.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "服务单价",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            unitPrices.take(4).forEach { price ->
                UnitPriceItem(
                    name = price.name,
                    price = price.price,
                    unit = price.unit
                )
            }
        }
    }
}

@Composable
private fun UnitPriceItem(name: String, price: BigDecimal, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "¥${price.format(4)}/$unit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TransparentPricingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "🎯 透明定价承诺",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "EvoMind 采用成本透明定价模式，您的订阅费完全基于实际算力成本计算。我们承诺：",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            TransparentPricingBullet("所有成本项目完全公开透明")
            TransparentPricingBullet("仅收取成本×2的合理费用")
            TransparentPricingBullet("无隐藏费用，无强制捆绑")
        }
    }
}

@Composable
private fun TransparentPricingBullet(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 扩展函数：格式化BigDecimal
private fun BigDecimal.format(scale: Int): String {
    return this.setScale(scale, java.math.RoundingMode.HALF_UP).toString()
}

// 格式化数字
private fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> "${number / 1_000_000}M"
        number >= 1_000 -> "${number / 1_000}K"
        else -> number.toString()
    }
}

// 单价数据类
data class UnitPriceItem(
    val name: String,
    val price: BigDecimal,
    val unit: String
)

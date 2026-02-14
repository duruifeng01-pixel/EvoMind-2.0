package com.evomind.ui.screens.ocr

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.evomind.ui.theme.EvoMindColors
import java.io.ByteArrayOutputStream

/**
 * OCR导入页面 - 截图识别导入信息源
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrImportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResult: (taskId: String) -> Unit,
    viewModel: OcrImportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPlatform by remember { mutableStateOf("xiaohongshu") }
    var showPlatformSelector by remember { mutableStateOf(false) }

    // 图片选择器
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel.onImageSelected(it)
        }
    }

    // 相机拍照
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            // 转换为URI
            val bytes = ByteArrayOutputStream().apply {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, this)
            }.toByteArray()
            // 临时保存并获取URI
            val tempUri = saveBytesToTempFile(context, bytes)
            selectedImageUri = tempUri
            viewModel.onImageSelected(tempUri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("截图导入信息源") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EvoMindColors.Surface
                )
            )
        },
        containerColor = EvoMindColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 说明卡片
            InfoCard()

            // 平台选择
            PlatformSelector(
                selectedPlatform = selectedPlatform,
                onPlatformSelected = { selectedPlatform = it },
                expanded = showPlatformSelector,
                onExpandedChange = { showPlatformSelector = it }
            )

            // 图片选择区域
            ImageSelectionArea(
                selectedImageUri = selectedImageUri,
                onGalleryClick = { imagePicker.launch("image/*") },
                onCameraClick = { cameraLauncher.launch(null) },
                onClearImage = {
                    selectedImageUri = null
                    viewModel.onImageCleared()
                }
            )

            // 限额提示
            ImportLimitHint()

            Spacer(modifier = Modifier.weight(1f))

            // 开始识别按钮
            Button(
                onClick = {
                    selectedImageUri?.let { uri ->
                        viewModel.startRecognition(uri, selectedPlatform) { taskId ->
                            onNavigateToResult(taskId)
                        }
                    }
                },
                enabled = selectedImageUri != null && uiState !is OcrImportUiState.Recognizing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EvoMindColors.Primary
                )
            ) {
                when (uiState) {
                    is OcrImportUiState.Recognizing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("识别中...")
                    }
                    else -> {
                        Icon(Icons.Default.Search, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始识别")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = EvoMindColors.Surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "如何导入信息源？",
                style = MaterialTheme.typography.titleMedium,
                color = EvoMindColors.TextPrimary
            )
            Text(
                text = "1. 在小红书/微信等平台截取关注列表\n" +
                       "2. 选择截图并点击开始识别\n" +
                       "3. 勾选要导入的博主，一键添加",
                style = MaterialTheme.typography.bodyMedium,
                color = EvoMindColors.TextSecondary,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlatformSelector(
    selectedPlatform: String,
    onPlatformSelected: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val platforms = listOf(
        Pair("xiaohongshu", "小红书"),
        Pair("weixin", "微信公众号"),
        Pair("douyin", "抖音"),
        Pair("zhihu", "知乎"),
        Pair("other", "其他平台")
    )

    val platformNames = mapOf(
        "xiaohongshu" to "小红书",
        "weixin" to "微信公众号",
        "douyin" to "抖音",
        "zhihu" to "知乎",
        "other" to "其他平台"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = platformNames[selectedPlatform] ?: "小红书",
            onValueChange = {},
            readOnly = true,
            label = { Text("截图来源") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EvoMindColors.Primary,
                focusedLabelColor = EvoMindColors.Primary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            platforms.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onPlatformSelected(value)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun ImageSelectionArea(
    selectedImageUri: Uri?,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onClearImage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = EvoMindColors.Surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                // 显示选中的图片
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "选中的截图",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // 清除按钮
                IconButton(
                    onClick = onClearImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(
                            color = EvoMindColors.Background.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        "清除",
                        tint = EvoMindColors.TextPrimary
                    )
                }
            } else {
                // 选择区域
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 相册选择
                    OutlinedButton(
                        onClick = onGalleryClick,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从相册选择")
                    }

                    // 相机拍照
                    OutlinedButton(
                        onClick = onCameraClick,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("拍照")
                    }

                    Text(
                        text = "支持小红书/微信/抖音等平台截图",
                        style = MaterialTheme.typography.bodySmall,
                        color = EvoMindColors.TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportLimitHint() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = EvoMindColors.Primary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = EvoMindColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "今日剩余导入次数: 20/20",
                style = MaterialTheme.typography.bodySmall,
                color = EvoMindColors.Primary
            )
        }
    }
}

// 辅助函数：保存字节到临时文件
private fun saveBytesToTempFile(context: android.content.Context, bytes: ByteArray): Uri {
    val file = java.io.File.createTempFile("camera_capture_", ".jpg", context.cacheDir)
    file.writeBytes(bytes)
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

package com.evomind.ui.screens.share

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareImageScreen(
    onNavigateBack: () -> Unit,
    viewModel: ShareImageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分享图") },
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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("模板") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("我的分享图") }
                )
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    when (selectedTab) {
                        0 -> TemplatesTab(
                            templates = uiState.templates,
                            onTemplateSelected = { templateId ->
                                viewModel.generateShareImage(
                                    templateId = templateId,
                                    content = mapOf(
                                        "userName" to "EvoMind用户",
                                        "stats" to mapOf(
                                            "cards" to 42,
                                            "hours" to 128.5,
                                            "days" to 30
                                        )
                                    )
                                )
                            },
                            generatedImage = uiState.generatedImage,
                            onSave = { imageUrl ->
                                viewModel.saveImageToGallery(context, imageUrl)
                            },
                            onShare = { imageUrl ->
                                viewModel.shareImage(context, imageUrl)
                            }
                        )
                        1 -> MyImagesTab(
                            images = uiState.myImages,
                            onLoadMore = { viewModel.loadMyImages() }
                        )
                    }
                }
            }
        }

        if (uiState.savedSuccess) {
            LaunchedEffect(Unit) {
                viewModel.clearSavedFlag()
            }
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("图片已保存到相册")
            }
        }
    }
}

@Composable
private fun TemplatesTab(
    templates: List<com.evomind.data.remote.dto.response.ShareTemplateDto>,
    onTemplateSelected: (Long) -> Unit,
    generatedImage: com.evomind.data.remote.dto.response.GeneratedShareImageDto?,
    onSave: (String) -> Unit,
    onShare: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "选择模板",
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(templates) { template ->
                    TemplateCard(
                        template = template,
                        onClick = { onTemplateSelected(template.id ?: 0) }
                    )
                }
            }
        }

        generatedImage?.let { image ->
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "预览",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (image.imageUrl != null) {
                            Text("分享图预览区域")
                        } else {
                            Text("暂无预览")
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSave(image.imageUrl ?: "") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("保存相册")
                    }
                    Button(
                        onClick = { onShare(image.imageUrl ?: "") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("分享")
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: com.evomind.data.remote.dto.response.ShareTemplateDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = template.name ?: "模板",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = template.name ?: "模板",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MyImagesTab(
    images: List<com.evomind.data.remote.dto.response.GeneratedShareImageDto>,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (images.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无分享图，快去生成吧！")
                }
            }
        } else {
            items(images) { image ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("分享图 #${image.id}")
                    }
                }
            }
        }
    }
}

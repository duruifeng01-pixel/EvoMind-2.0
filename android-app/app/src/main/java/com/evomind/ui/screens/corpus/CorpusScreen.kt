package com.evomind.ui.screens.corpus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evomind.domain.model.UserCorpus
import com.evomind.ui.viewmodel.CorpusTab
import com.evomind.ui.viewmodel.CorpusUiState
import com.evomind.ui.viewmodel.CorpusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorpusScreen(
    viewModel: CorpusViewModel = hiltViewModel(),
    onCorpusClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var searchKeyword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchBar(
                    query = searchKeyword,
                    onQueryChange = { searchKeyword = it },
                    onSearch = { viewModel.searchCorpus(it) },
                    active = false,
                    onActiveChange = { },
                    placeholder = { Text("搜索语料...") },
                    leadingIcon = {
                        IconButton(onClick = { 
                            showSearchBar = false
                            searchKeyword = ""
                            viewModel.loadCorpus()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    trailingIcon = {
                        if (searchKeyword.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchKeyword = ""
                                viewModel.loadCorpus()
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "清空")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { }
            } else {
                TopAppBar(
                    title = { Text("我的语料库") },
                    actions = {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Card
            uiState.stats?.let { stats ->
                StatsCard(
                    total = stats.total,
                    insightCount = stats.socraticInsightCount,
                    noteCount = stats.userNoteCount,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Category Tabs
            CategoryTabs(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )

            // Content
            if (uiState.isLoading && uiState.corpusList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.corpusList.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pinned items first
                    val pinnedItems = uiState.corpusList.filter { it.isPinned }
                    val unpinnedItems = uiState.corpusList.filter { !it.isPinned }

                    if (pinnedItems.isNotEmpty()) {
                        item {
                            Text(
                                text = "置顶",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(pinnedItems, key = { "pinned_${it.id}" }) { corpus ->
                            CorpusItemCard(
                                corpus = corpus,
                                onClick = { onCorpusClick(corpus.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(corpus.id) },
                                onPinClick = { viewModel.togglePin(corpus.id) },
                                onArchiveClick = { viewModel.archiveCorpus(corpus.id) },
                                onDeleteClick = { viewModel.deleteCorpus(corpus.id) }
                            )
                        }
                    }

                    if (unpinnedItems.isNotEmpty()) {
                        if (pinnedItems.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        items(unpinnedItems, key = { it.id }) { corpus ->
                            CorpusItemCard(
                                corpus = corpus,
                                onClick = { onCorpusClick(corpus.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(corpus.id) },
                                onPinClick = { viewModel.togglePin(corpus.id) },
                                onArchiveClick = { viewModel.archiveCorpus(corpus.id) },
                                onDeleteClick = { viewModel.deleteCorpus(corpus.id) }
                            )
                        }
                    }

                    // Load more
                    if (uiState.hasMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { viewModel.loadMore() }) {
                                    Text("加载更多")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(
    total: Long,
    insightCount: Long,
    noteCount: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "总计", value = total.toString())
            StatItem(label = "洞察", value = insightCount.toString())
            StatItem(label = "笔记", value = noteCount.toString())
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CategoryTabs(
    selectedTab: CorpusTab,
    onTabSelected: (CorpusTab) -> Unit
) {
    val tabs = listOf(
        CorpusTab.ALL to "全部",
        CorpusTab.TYPE_SOCRATIC to "洞察",
        CorpusTab.TYPE_NOTE to "笔记",
        CorpusTab.TYPE_AI_SUMMARY to "AI总结",
        CorpusTab.FAVORITES to "收藏"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs) { (tab, label) ->
            FilterChip(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                label = { Text(label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CorpusItemCard(
    corpus: UserCorpus,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPinClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (corpus.isPinned) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (corpus.isPinned) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "置顶",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = corpus.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (corpus.isPinned) "取消置顶" else "置顶") },
                            onClick = { 
                                onPinClick()
                                showMenu = false
                            },
                            leadingIcon = { 
                                Icon(Icons.Default.PushPin, contentDescription = null) 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (corpus.isFavorite) "取消收藏" else "收藏") },
                            onClick = { 
                                onFavoriteClick()
                                showMenu = false
                            },
                            leadingIcon = { 
                                Icon(
                                    if (corpus.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null
                                ) 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("归档") },
                            onClick = { 
                                onArchiveClick()
                                showMenu = false
                            },
                            leadingIcon = { 
                                Icon(Icons.Default.Archive, contentDescription = null) 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = { 
                                onDeleteClick()
                                showMenu = false
                            },
                            leadingIcon = { 
                                Icon(Icons.Default.Delete, contentDescription = null) 
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = corpus.getDisplaySummary(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = corpus.getTypeDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Keywords
                if (corpus.keywords.isNotBlank()) {
                    Text(
                        text = corpus.getKeywordsList().take(3).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.LibraryBooks,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无语料",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "开始你的学习之旅，记录洞察与思考",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

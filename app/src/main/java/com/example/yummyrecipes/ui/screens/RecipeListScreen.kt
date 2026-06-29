package com.example.yummyrecipes.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yummyrecipes.ui.components.EmptyState
import com.example.yummyrecipes.ui.components.RecipeCard
import com.example.yummyrecipes.ui.theme.*
import com.example.yummyrecipes.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDiscover: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var showSearch by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.resetSaveSuccess() }
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackBarHostState.showSnackbar("✓ 操作成功")
            viewModel.resetSaveSuccess()
        }
    }

    // 下拉刷新完成
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            // 刷新完成
            viewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            if (showSearch) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                                // 防抖搜索
                                viewModel.searchRecipesWithDebounce(it)
                            },
                            placeholder = { Text("搜索食谱…（防抖 300ms）", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Orange500,
                                unfocusedBorderColor = Orange200,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = { searchText = ""; viewModel.clearSearch() }) {
                                        Icon(Icons.Default.Close, "清除", tint = Orange500)
                                    }
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { showSearch = false; searchText = ""; viewModel.clearSearch() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "关闭")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🍳", fontSize = 22.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "馋趣食谱",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "· ${uiState.recipes.size} 道美味",
                                style = MaterialTheme.typography.bodySmall,
                                color = Orange500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    actions = {
                        // 网络状态指示
                        if (!uiState.isNetworkAvailable) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        IconButton(onClick = onNavigateToDiscover) {
                            Icon(Icons.Default.Explore, "发现食谱", tint = Orange500, modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, "设置", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, "搜索", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                shape = CircleShape,
                containerColor = Orange500,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier.shadow(12.dp, CircleShape)
            ) {
                Icon(Icons.Default.Add, "新增食谱", modifier = Modifier.size(28.dp))
            }
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface
                    )
                ) {
                    Text(
                        data.visuals.message,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 离线提示
            if (!uiState.isNetworkAvailable && uiState.recipes.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("当前无网络连接，部分功能不可用", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
            ) {
                // 欢迎横幅（精简版）
                if (!showSearch && uiState.recipes.isNotEmpty()) {
                    item {
                        WelcomeBanner(
                            totalRecipes = uiState.recipes.size,
                            favoriteCount = uiState.recipes.count { it.isFavorite },
                            onClick = onNavigateToDiscover
                        )
                    }
                }

                // 统计数据卡片（选做：联合查询结果展示）
                if (uiState.recipeStats.isNotEmpty() && !showSearch) {
                    item {
                        StatsRow(uiState.recipeStats)
                    }
                }

                // 分类横向滚动
                if (uiState.categories.isNotEmpty()) {
                    item {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            item {
                                val isSelected = uiState.selectedCategoryId == -1L
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectCategory(-1) },
                                    label = {
                                        Text("🍽️ 全部", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Orange500, selectedLabelColor = Color.White),
                                    shape = RoundedCornerShape(22.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                            items(uiState.categories) { cat ->
                                val idx = ((cat.id - 1) % CategoryColors.size).toInt()
                                val catColor = Color(CategoryColors[idx])
                                val isSelected = uiState.selectedCategoryId == cat.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectCategory(cat.id) },
                                    label = { Text(cat.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
                                    leadingIcon = { Box(Modifier.size(8.dp).clip(CircleShape).background(catColor)) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = catColor.copy(alpha = 0.15f), selectedLabelColor = catColor),
                                    shape = RoundedCornerShape(22.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                        }
                    }
                }

                // 列表标题
                item {
                    Text(
                        if (uiState.selectedCategoryId == -1L) "全部食谱" else "分类食谱",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 食谱列表
                when {
                    uiState.isLoading -> {
                        item {
                            Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Orange500, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(16.dp))
                                    Text("正在加载美味…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    uiState.recipes.isEmpty() -> {
                        item {
                            EmptyState(
                                icon = Icons.Default.RestaurantMenu,
                                title = "还没有食谱",
                                subtitle = "点击下方 ➕ 按钮添加你的\n第一道美味食谱吧 🍽️"
                            )
                        }
                    }
                    else -> {
                        items(uiState.recipes, key = { it.id }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onDetail = { onNavigateToDetail(recipe.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(recipe) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

/** 分类统计卡片行 */
@Composable
private fun StatsRow(stats: List<com.example.yummyrecipes.data.entity.RecipeStat>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(stats.take(6)) { stat ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${stat.recipeCount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Orange500
                    )
                    Text(
                        stat.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeBanner(totalRecipes: Int, favoriteCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Orange500, Color(0xFFFF8C5A), Color(0xFFFFB088))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("👨‍🍳 开始烹饪之旅吧！", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("探索美味，记录每一道心动食谱", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalRecipes", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                        Text("食谱", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$favoriteCount", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                        Text("收藏", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

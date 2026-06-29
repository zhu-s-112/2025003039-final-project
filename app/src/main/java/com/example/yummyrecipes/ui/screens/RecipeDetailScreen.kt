package com.example.yummyrecipes.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yummyrecipes.data.entity.RecipeEntity
import com.example.yummyrecipes.ui.theme.*
import com.example.yummyrecipes.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: RecipeViewModel,
    recipeId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val recipe = uiState.recipes.find { it.id == recipeId }

    if (recipe == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Orange500, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("加载食谱…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val diffColor = when (recipe.difficulty) {
        0 -> DifficultyEasy
        2 -> DifficultyHard
        else -> DifficultyMedium
    }

    val diffLabel = when (recipe.difficulty) {
        0 -> "简单"
        2 -> "困难"
        else -> "中等"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 收藏按钮
                    IconButton(onClick = { viewModel.toggleFavorite(recipe) }) {
                        Icon(
                            if (recipe.isFavorite)
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
                            "收藏",
                            tint = if (recipe.isFavorite) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { onNavigateToEdit(recipe.id) }) {
                        Icon(
                            Icons.Default.Edit,
                            "编辑",
                            tint = Orange500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 渐变头部区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Orange500,
                                Color(0xFFFF8C5A),
                                Orange100
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 装饰背景圆圈
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .align(Alignment.TopEnd)
                        .offset(x = 40.dp, y = (-30).dp)
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.BottomStart)
                        .offset(x = (-20).dp, y = 20.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 食谱图标
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.RestaurantMenu,
                                null,
                                modifier = Modifier.size(38.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        recipe.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // 内容区
            Column(modifier = Modifier.padding(24.dp)) {
                // 信息标签卡片
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.offset(y = (-30).dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfoBadge(
                            icon = Icons.Default.Timer,
                            label = "总时间",
                            value = "${recipe.prepTime + recipe.cookTime}分钟",
                            color = Orange500
                        )
                        InfoBadge(
                            icon = Icons.Default.Speed,
                            label = "难度",
                            value = diffLabel,
                            color = diffColor
                        )
                        InfoBadge(
                            icon = Icons.Default.People,
                            label = "份量",
                            value = "${recipe.servings}人份",
                            color = Blueberry
                        )
                    }
                }

                // 简介
                if (recipe.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    SectionTitle("📝 简介")
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Orange500.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            recipe.description,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                    }
                }

                // 食材
                if (recipe.ingredients.isNotBlank()) {
                    Spacer(Modifier.height(28.dp))
                    SectionTitle("🛒 食材")
                    Spacer(Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            recipe.ingredients.split("\n")
                                .filter { it.isNotBlank() }
                                .forEach { IngredientsRow(it) }
                        }
                    }
                }

                // 步骤
                if (recipe.steps.isNotBlank()) {
                    Spacer(Modifier.height(28.dp))
                    SectionTitle("👩‍🍳 烹饪步骤")
                    Spacer(Modifier.height(16.dp))

                    val stepsList = recipe.steps.split("\n").filter { it.isNotBlank() }
                    stepsList.forEachIndexed { idx, step ->
                        StepRow(idx + 1, step, stepsList.size)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun InfoBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(22.dp), tint = color)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IngredientsRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Orange500,
                            Orange400
                        )
                    )
                )
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StepRow(number: Int, text: String, totalSteps: Int) {
    val stepColor = when {
        number == 1 -> DifficultyEasy
        number == totalSteps -> Orange500
        else -> Blueberry
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = stepColor,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "$number",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
            // 连接线
            if (number < totalSteps) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(stepColor.copy(alpha = 0.2f))
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = stepColor.copy(alpha = 0.06f),
            modifier = Modifier.weight(1f).offset(y = 2.dp)
        ) {
            Text(
                text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}

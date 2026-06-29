package com.example.yummyrecipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yummyrecipes.ui.theme.*
import com.example.yummyrecipes.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeScreen(
    viewModel: RecipeViewModel,
    recipeId: Long?,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val isEdit = recipeId != null

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    var categoryId by remember { mutableLongStateOf(1L) } // 默认选中第1个分类
    var prepTime by remember { mutableIntStateOf(15) }
    var cookTime by remember { mutableIntStateOf(15) }
    var servings by remember { mutableIntStateOf(2) }
    var difficulty by remember { mutableIntStateOf(1) }

    // 输入验证状态
    var validationError by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var ingredientsError by remember { mutableStateOf<String?>(null) }
    var stepsError by remember { mutableStateOf<String?>(null) }

    /** 执行输入验证，返回 true 表示通过 */
    fun validate(): Boolean {
        var valid = true
        when {
            title.isBlank() -> {
                titleError = "食谱名称不能为空"
                valid = false
            }
            title.trim().length < 2 -> {
                titleError = "名称至少需要2个字符"
                valid = false
            }
            else -> titleError = null
        }
        when {
            ingredients.isBlank() -> {
                ingredientsError = "请填写食材清单"
                valid = false
            }
            ingredients.trim().length < 5 -> {
                ingredientsError = "食材描述过于简短"
                valid = false
            }
            else -> ingredientsError = null
        }
        when {
            steps.isBlank() -> {
                stepsError = "请填写烹饪步骤"
                valid = false
            }
            steps.trim().length < 10 -> {
                stepsError = "步骤描述过于简短"
                valid = false
            }
            else -> stepsError = null
        }
        if (!valid) {
            validationError = "请完善必填信息后再保存"
        } else {
            validationError = null
        }
        return valid
    }

    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            uiState.recipes.find { it.id == recipeId }?.let { r ->
                title = r.title; desc = r.description; ingredients = r.ingredients
                steps = r.steps; categoryId = r.categoryId; prepTime = r.prepTime
                cookTime = r.cookTime; servings = r.servings; difficulty = r.difficulty
                viewModel.startEditRecipe(r)
            }
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackBarHostState.showSnackbar(if (isEdit) "✓ 更新成功" else "✓ 创建成功")
            viewModel.resetSaveSuccess(); onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "✏️ 编辑食谱" else "✨ 新建食谱",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        bottomBar = {
            // ---------- 底部固定的保存按钮 ----------
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = {
                        if (!validate()) return@Button
                        if (isEdit) {
                            uiState.editingRecipe?.let { r ->
                                viewModel.updateRecipe(
                                    r.copy(
                                        title = title.trim(), description = desc.trim(),
                                        ingredients = ingredients.trim(), steps = steps.trim(),
                                        categoryId = categoryId, prepTime = prepTime,
                                        cookTime = cookTime, servings = servings, difficulty = difficulty
                                    )
                                )
                            }
                        } else {
                            viewModel.addRecipe(
                                title = title.trim(), description = desc.trim(),
                                ingredients = ingredients.trim(), steps = steps.trim(),
                                categoryId = categoryId, prepTime = prepTime,
                                cookTime = cookTime, servings = servings, difficulty = difficulty
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(52.dp),
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange500,
                        disabledContainerColor = Orange200.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isEdit) "保存修改" else "创建食谱",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // 卡片式输入区
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    SectionLabel("食谱名称", Icons.Default.RestaurantMenu)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; titleError = null },
                        placeholder = { Text("如：红烧排骨、提拉米苏…") },
                        singleLine = true,
                        isError = titleError != null,
                        supportingText = titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = inputColors()
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    SectionLabel("简介", Icons.Default.Description)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        placeholder = { Text("简单描述这道食谱（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = inputColors()
                    )
                }
            }

            // 分类选择
            if (uiState.categories.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        SectionLabel("分类", Icons.Default.Category)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.categories.forEach { cat ->
                                val catColor = Color(
                                    CategoryColors[((cat.id - 1) % CategoryColors.size).toInt()]
                                )
                                FilterChip(
                                    selected = categoryId == cat.id,
                                    onClick = { categoryId = cat.id },
                                    label = {
                                        Text(
                                            cat.name,
                                            fontWeight = if (categoryId == cat.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = catColor.copy(alpha = 0.15f),
                                        selectedLabelColor = catColor
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 食材
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    SectionLabel("食材清单", Icons.Default.ShoppingCart)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ingredients,
                        onValueChange = { ingredients = it; ingredientsError = null },
                        placeholder = {
                            Text("每行一个食材，如：\n鸡蛋 2个\n面粉 200g\n牛奶 250ml")
                        },
                        isError = ingredientsError != null,
                        supportingText = ingredientsError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(14.dp),
                        colors = inputColors()
                    )
                }
            }

            // 步骤
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    SectionLabel("烹饪步骤", Icons.AutoMirrored.Filled.ListAlt)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = steps,
                        onValueChange = { steps = it; stepsError = null },
                        placeholder = {
                            Text("每行一个步骤，如：\n1. 将面粉过筛\n2. 加入鸡蛋搅拌\n3. 烤箱预热180度")
                        },
                        isError = stepsError != null,
                        supportingText = stepsError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        maxLines = 12,
                        shape = RoundedCornerShape(14.dp),
                        colors = inputColors()
                    )
                }
            }

            // 时间 & 份量
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    SectionLabel("时间与份量", Icons.Default.Timer)
                    Spacer(Modifier.height(12.dp))
                    NumberRow("准备时间", prepTime, 5..120, "分钟") { prepTime = it }
                    Divider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    NumberRow("烹饪时间", cookTime, 5..240, "分钟") { cookTime = it }
                    Divider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    NumberRow("份量", servings, 1..20, "人份") { servings = it }
                }
            }

            // 难度
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    SectionLabel("难度", Icons.Default.Speed)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            Triple(0, "🟢 简单", DifficultyEasy),
                            Triple(1, "🟡 中等", DifficultyMedium),
                            Triple(2, "🔴 困难", DifficultyHard)
                        ).forEach { (v, label, color) ->
                            FilterChip(
                                selected = difficulty == v,
                                onClick = { difficulty = v },
                                label = {
                                    Text(
                                        label,
                                        fontWeight = if (difficulty == v) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.15f),
                                    selectedLabelColor = color
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // 验证错误提示
            if (validationError != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        validationError!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            uiState.errorMessage?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = Orange500.copy(alpha = 0.12f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Orange500, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NumberRow(
    label: String,
    value: Int,
    range: IntRange,
    unit: String,
    onChange: (Int) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Orange500.copy(alpha = 0.1f),
            modifier = Modifier.size(32.dp)
        ) {
            IconButton(
                onClick = { if (value > range.first) onChange(value - 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, "减少", modifier = Modifier.size(16.dp), tint = Orange500)
            }
        }
        Text(
            "$value $unit",
            modifier = Modifier.padding(horizontal = 14.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Orange500,
            modifier = Modifier.size(32.dp)
        ) {
            IconButton(
                onClick = { if (value < range.last) onChange(value + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, "增加", modifier = Modifier.size(16.dp), tint = Color.White)
            }
        }
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Orange500,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    focusedContainerColor = Orange500.copy(alpha = 0.03f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
)

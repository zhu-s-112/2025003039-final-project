package com.example.yummyrecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.yummyrecipes.data.entity.CategoryEntity
import com.example.yummyrecipes.data.entity.RecipeEntity
import com.example.yummyrecipes.data.network.NetworkUtils
import com.example.yummyrecipes.data.repository.RecipeRepository
import com.example.yummyrecipes.data.preferences.UserPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val repository: RecipeRepository,
    private val userPreferences: UserPreferences,
    private val context: android.content.Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // 搜索防抖 Job
    private var searchJob: Job? = null

    // 保存全部食谱，用于分类筛选时过滤
    private var _allRecipes: List<RecipeEntity> = emptyList()

    init {
        loadCategories()
        loadRecipes()
        loadPreferences()
        loadStats()
        observeNetwork()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.allCategories.collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            repository.allRecipes.collect { recipes ->
                _allRecipes = recipes
                _uiState.update {
                    it.copy(
                        recipes = filterByCategory(recipes, it.selectedCategoryId),
                        isLoading = false,
                        totalRecipes = recipes.size,
                        favoriteCount = recipes.count { r -> r.isFavorite }
                    )
                }
            }
        }
    }

    /** 根据分类 ID 过滤食谱（-1 表示全部，0 表示未分类，>0 表示具体分类） */
    private fun filterByCategory(recipes: List<RecipeEntity>, categoryId: Long): List<RecipeEntity> {
        return when (categoryId) {
            -1L -> recipes                 // 全部
            0L  -> recipes.filter { it.categoryId == 0L }  // 未分类
            else -> recipes.filter { it.categoryId == categoryId }
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferences.isDarkMode.collect { enabled ->
                _uiState.update { it.copy(isDarkMode = enabled) }
            }
        }
    }

    /** 加载分组统计数据 */
    private fun loadStats() {
        viewModelScope.launch {
            repository.recipeCountPerCategory.collect { stats ->
                _uiState.update { it.copy(recipeStats = stats) }
            }
        }
    }

    /** 监听网络状态 */
    private fun observeNetwork() {
        viewModelScope.launch {
            NetworkUtils.networkStateFlow(context).distinctUntilChanged().collect { available ->
                _uiState.update { it.copy(isNetworkAvailable = available) }
            }
        }
    }

    // ==================== 本地食谱操作 ====================

    fun selectCategory(categoryId: Long) {
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                recipes = filterByCategory(_allRecipes, categoryId)
            )
        }
    }

    /** 搜索防抖：300ms 内连续输入只触发最后一次 */
    @OptIn(FlowPreview::class)
    fun searchRecipesWithDebounce(query: String) {
        val catId = _uiState.value.selectedCategoryId
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            val flow = if (query.isBlank()) {
                repository.allRecipes
            } else {
                repository.searchRecipes(query)
            }
            flow.collect { recipes ->
                if (query.isBlank()) _allRecipes = recipes
                _uiState.update { it.copy(recipes = filterByCategory(recipes, catId)) }
            }
        }
    }

    fun clearSearch() {
        val catId = _uiState.value.selectedCategoryId
        _uiState.update { it.copy(searchQuery = "") }
        viewModelScope.launch {
            repository.allRecipes.collect { recipes ->
                _allRecipes = recipes
                _uiState.update { it.copy(recipes = filterByCategory(recipes, catId)) }
            }
        }
    }

    fun startEditRecipe(recipe: RecipeEntity) {
        _uiState.update { it.copy(editingRecipe = recipe) }
    }

    fun addRecipe(
        title: String, description: String, ingredients: String, steps: String,
        categoryId: Long, prepTime: Int, cookTime: Int, servings: Int, difficulty: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.insertRecipe(
                    RecipeEntity(
                        title = title, description = description,
                        ingredients = ingredients, steps = steps,
                        categoryId = categoryId, prepTime = prepTime,
                        cookTime = cookTime, servings = servings,
                        difficulty = difficulty
                    )
                )
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "保存失败: ${e.message}") }
            }
        }
    }

    fun updateRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.updateRecipe(recipe)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true, editingRecipe = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "更新失败: ${e.message}") }
            }
        }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch { repository.deleteRecipe(recipe) }
    }

    fun toggleFavorite(recipe: RecipeEntity) {
        viewModelScope.launch { repository.updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite)) }
    }

    fun addCategory(name: String, color: Long) {
        viewModelScope.launch { repository.insertCategory(CategoryEntity(name = name, color = color)) }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    // ==================== 下拉刷新 ====================

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            // 短暂延迟模拟刷新
            kotlinx.coroutines.delay(600)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    // ==================== DataStore 偏好持久化 ====================

    fun toggleDarkMode() {
        val newValue = !_uiState.value.isDarkMode
        _uiState.update { it.copy(isDarkMode = newValue) }
        viewModelScope.launch { userPreferences.setDarkMode(newValue) }
    }

    // ==================== JSON 导出 ====================

    fun exportRecipesAsJson() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val json = repository.exportRecipesAsJson()
                _uiState.update { it.copy(isExporting = false, exportSuccess = true, errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, errorMessage = "导出失败: ${e.message}") }
            }
        }
    }

    fun resetExportSuccess() {
        _uiState.update { it.copy(exportSuccess = false) }
    }

    // ==================== 网络请求 ====================

    fun fetchRandomMeal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDiscoverLoading = true, discoverError = null, discoverRemoteMeal = null) }
            try {
                val meal = repository.fetchRandomMeal()
                if (meal != null) {
                    _uiState.update { it.copy(isDiscoverLoading = false, discoverRemoteMeal = meal) }
                } else {
                    _uiState.update { it.copy(isDiscoverLoading = false, discoverError = "未获取到食谱，请重试") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isDiscoverLoading = false, discoverError = "网络请求失败: ${e.message ?: "未知错误"}")
                }
            }
        }
    }

    fun fetchMealsByCategory(category: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDiscoverLoading = true, discoverError = null) }
            try {
                val meals = repository.fetchMealsByCategory(category)
                _uiState.update { it.copy(isDiscoverLoading = false, discoverMeals = meals) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDiscoverLoading = false, discoverError = "加载失败: ${e.message}") }
            }
        }
    }

    fun searchOnlineMeals(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDiscoverLoading = true, discoverError = null) }
            try {
                val meals = repository.searchOnlineMeals(query)
                _uiState.update { it.copy(isDiscoverLoading = false, discoverMeals = meals) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDiscoverLoading = false, discoverError = "搜索失败") }
            }
        }
    }

    fun importMeal(remoteMeal: com.example.yummyrecipes.data.network.RemoteMeal) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                repository.importRemoteMeal(remoteMeal)
                _uiState.update { it.copy(isImporting = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, errorMessage = "导入失败: ${e.message}") }
            }
        }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearDiscoverError() {
        _uiState.update { it.copy(discoverError = null) }
    }

    fun clearDiscoverResults() {
        _uiState.update { it.copy(discoverMeals = emptyList(), discoverRemoteMeal = null, discoverError = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ==================== Factory ====================

    class Factory(
        private val repository: RecipeRepository,
        private val userPreferences: UserPreferences,
        private val context: android.content.Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecipeViewModel(repository, userPreferences, context) as T
        }
    }
}

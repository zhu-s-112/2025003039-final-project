package com.example.yummyrecipes.viewmodel

import com.example.yummyrecipes.data.entity.CategoryEntity
import com.example.yummyrecipes.data.entity.RecipeEntity
import com.example.yummyrecipes.data.entity.RecipeStat
import com.example.yummyrecipes.data.network.RemoteMeal
import com.example.yummyrecipes.data.network.RemoteMealBrief

/**
 * UiState — 统一管理所有 UI 状态
 * 涵盖：本地食谱 CRUD、网络发现、用户偏好、统计数据、网络状态、导出
 */
data class RecipeUiState(
    // --- 本地食谱 ---
    val recipes: List<RecipeEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long = -1, // -1 = 全部, 0 = 未分类, >0 = 具体分类
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val editingRecipe: RecipeEntity? = null,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    // --- 网络发现 ---
    val discoverMeals: List<RemoteMealBrief> = emptyList(),
    val discoverRemoteMeal: RemoteMeal? = null,
    val isDiscoverLoading: Boolean = false,
    val isImporting: Boolean = false,
    val discoverError: String? = null,
    // --- 用户偏好 ---
    val isDarkMode: Boolean = false,
    // --- 选做功能：统计 & 网络 & 导出 ---
    val recipeStats: List<RecipeStat> = emptyList(),
    val isNetworkAvailable: Boolean = true,
    val isRefreshing: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val totalRecipes: Int = 0,
    val favoriteCount: Int = 0
)

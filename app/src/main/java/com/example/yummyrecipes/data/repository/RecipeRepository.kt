package com.example.yummyrecipes.data.repository

import com.example.yummyrecipes.data.dao.CategoryDao
import com.example.yummyrecipes.data.dao.RecipeDao
import com.example.yummyrecipes.data.entity.CategoryEntity
import com.example.yummyrecipes.data.entity.RecipeEntity
import com.example.yummyrecipes.data.entity.RecipeStat
import com.example.yummyrecipes.data.network.ApiClient
import com.example.yummyrecipes.data.network.RemoteMeal
import com.example.yummyrecipes.data.network.RemoteMealBrief
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository — 隔离 ViewModel 与数据源（Room DAO + 网络 API）
 * ViewModel 只通过 Repository 访问数据，不直接调用 DAO 或网络客户端。
 */
class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val categoryDao: CategoryDao
) {
    // ==================== 本地 Room 操作 ====================

    val allRecipes: Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()

    fun getRecipesByCategory(categoryId: Long): Flow<List<RecipeEntity>> =
        recipeDao.getRecipesByCategory(categoryId)

    fun searchRecipes(query: String): Flow<List<RecipeEntity>> =
        recipeDao.searchRecipes(query)

    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    /** 联合查询：按分类统计食谱数量 */
    val recipeCountPerCategory: Flow<List<RecipeStat>> =
        recipeDao.getRecipeCountPerCategory()

    suspend fun getRecipeById(id: Long): RecipeEntity? =
        recipeDao.getRecipeById(id)

    suspend fun insertRecipe(recipe: RecipeEntity): Long =
        recipeDao.insert(recipe)

    suspend fun updateRecipe(recipe: RecipeEntity) =
        recipeDao.update(recipe)

    suspend fun deleteRecipe(recipe: RecipeEntity) =
        recipeDao.delete(recipe)

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insert(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.delete(category)

    suspend fun getAllRecipesList(): List<RecipeEntity> {
        // 获取当前全量列表（非 Flow，用于导出等一次性操作）
        return recipeDao.getAllRecipes().first()
    }

    // ==================== JSON 导出 ====================

    /** 将所有食谱导出为 JSON 字符串 */
    suspend fun exportRecipesAsJson(): String {
        val recipes = recipeDao.getAllRecipes().first()
        val sb = StringBuilder()
        sb.appendLine("[")
        recipes.forEachIndexed { i, r ->
            sb.append("  {")
            sb.append("\"title\":${r.title.toJsonString()},")
            sb.append("\"description\":${r.description.toJsonString()},")
            sb.append("\"ingredients\":${r.ingredients.toJsonString()},")
            sb.append("\"steps\":${r.steps.toJsonString()},")
            sb.append("\"categoryId\":${r.categoryId},")
            sb.append("\"prepTime\":${r.prepTime},")
            sb.append("\"cookTime\":${r.cookTime},")
            sb.append("\"servings\":${r.servings},")
            sb.append("\"difficulty\":${r.difficulty}")
            sb.append("}")
            if (i < recipes.lastIndex) sb.append(",")
            sb.appendLine()
        }
        sb.appendLine("]")
        return sb.toString()
    }

    // ==================== 网络请求 ====================

    /** 获取随机食谱 */
    suspend fun fetchRandomMeal(): RemoteMeal? {
        val response = ApiClient.instance.getRandomMeal()
        return response.meals?.firstOrNull()
    }

    /** 按分类浏览网络食谱 */
    suspend fun fetchMealsByCategory(category: String): List<RemoteMealBrief> {
        val response = ApiClient.instance.filterByCategory(category)
        return response.meals ?: emptyList()
    }

    /** 搜索网络食谱 */
    suspend fun searchOnlineMeals(query: String): List<RemoteMealBrief> {
        val response = ApiClient.instance.searchMeals(query)
        return response.meals?.map {
            RemoteMealBrief(it.id, it.name, it.thumbnail)
        } ?: emptyList()
    }

    /** 将网络食谱转换为本地 Entity 并插入 */
    suspend fun importRemoteMeal(remoteMeal: RemoteMeal) {
        recipeDao.insert(
            RecipeEntity(
                title = remoteMeal.name,
                description = remoteMeal.area?.let { area ->
                    "${area}料理 · 分类: ${remoteMeal.category ?: "未知"}"
                } ?: "",
                ingredients = remoteMeal.ingredientsFormatted(),
                steps = remoteMeal.instructions ?: "",
                prepTime = 10,
                cookTime = 30,
                difficulty = 1
            )
        )
    }
}

/** 简单 JSON 字符串转义（零依赖） */
private fun String.toJsonString(): String {
    return "\"" + this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t") + "\""
}


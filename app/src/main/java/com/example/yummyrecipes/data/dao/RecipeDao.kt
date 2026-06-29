package com.example.yummyrecipes.data.dao

import androidx.room.*
import com.example.yummyrecipes.data.entity.RecipeEntity
import com.example.yummyrecipes.data.entity.RecipeStat
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY is_favorite DESC, created_at DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE categoryId = :categoryId ORDER BY is_favorite DESC, created_at DESC")
    fun getRecipesByCategory(categoryId: Long): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' OR ingredients LIKE '%' || :query || '%'")
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE is_favorite = 1")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    /** 联合查询：食谱数量按分类统计 (LEFT JOIN 方式) */
    @Query("""
        SELECT c.id AS categoryId, c.name AS categoryName,
               COUNT(r.id) AS recipeCount
        FROM categories c
        LEFT JOIN recipes r ON r.categoryId = c.id
        GROUP BY c.id, c.name
        ORDER BY recipeCount DESC
    """)
    fun getRecipeCountPerCategory(): Flow<List<RecipeStat>>

    /** 按烹饪难度筛选并排序 */
    @Query("SELECT * FROM recipes WHERE difficulty = :level ORDER BY created_at DESC")
    fun getRecipesByDifficulty(level: Int): Flow<List<RecipeEntity>>

    /** 按烹饪时间筛选（总时间 ≤ maxMinutes） */
    @Query("SELECT * FROM recipes WHERE (prep_time + cook_time) <= :maxMinutes ORDER BY created_at DESC")
    fun getRecipesByMaxTime(maxMinutes: Int): Flow<List<RecipeEntity>>

    /** 按标题排序（A-Z） */
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun getAllRecipesSortedByTitle(): Flow<List<RecipeEntity>>

    /** 按创建时间排序（最新在前） */
    @Query("SELECT * FROM recipes ORDER BY created_at DESC")
    fun getAllRecipesSortedByTime(): Flow<List<RecipeEntity>>

    /** 按收藏状态排序（收藏在前） */
    @Query("SELECT * FROM recipes ORDER BY is_favorite DESC, created_at DESC")
    fun getAllRecipesSortedByFavorite(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Delete
    suspend fun delete(recipe: RecipeEntity)

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()

    /** 批量插入（用于导入） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>): List<Long>
}

package com.example.yummyrecipes.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {
    /** 获取随机食谱 */
    @GET("random.php")
    suspend fun getRandomMeal(): MealResponse

    /** 按名称搜索食谱 */
    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): MealResponse

    /** 获取所有分类 */
    @GET("categories.php")
    suspend fun getCategories(): CategoryResponse

    /** 按分类筛选 */
    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealFilterResponse
}

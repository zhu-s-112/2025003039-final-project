package com.example.yummyrecipes.data.entity

/**
 * 分组统计结果 — 不存入数据库，仅用于 Room 查询返回。
 * 与 LEFT JOIN + GROUP BY 查询的列别名一一对应。
 */
data class RecipeStat(
    val categoryId: Long,
    val categoryName: String,
    val recipeCount: Int
)

package com.example.yummyrecipes.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val ingredients: String = "",
    val steps: String = "",
    val categoryId: Long = 0,
    @ColumnInfo(name = "image_url") val imageUrl: String = "",
    @ColumnInfo(name = "prep_time") val prepTime: Int = 0,
    @ColumnInfo(name = "cook_time") val cookTime: Int = 0,
    val servings: Int = 2,
    val difficulty: Int = 1,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

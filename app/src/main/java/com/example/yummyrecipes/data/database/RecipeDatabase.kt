package com.example.yummyrecipes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.yummyrecipes.data.dao.CategoryDao
import com.example.yummyrecipes.data.dao.RecipeDao
import com.example.yummyrecipes.data.entity.CategoryEntity
import com.example.yummyrecipes.data.entity.RecipeEntity

/**
 * 数据库迁移示例：
 * v1 → v2: recipes 表新增 image_url 列，categoryId 重命名
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 示例：为 recipes 表添加 image_url 列（如果旧表没有）
        // 注：首次创建即为 v2，此迁移主要展示 Room 数据库升级能力
    }
}

/** 预设的默认分类 */
private val DEFAULT_CATEGORIES = listOf(
    CategoryEntity(name = "中餐", color = 0xFFFF6B35, icon = "chinese"),
    CategoryEntity(name = "西餐", color = 0xFFE91E63, icon = "western"),
    CategoryEntity(name = "甜点", color = 0xFF4CAF50, icon = "dessert"),
    CategoryEntity(name = "家常菜", color = 0xFF5C6BC0, icon = "home"),
    CategoryEntity(name = "汤羹", color = 0xFFFFC107, icon = "soup"),
    CategoryEntity(name = "面食", color = 0xFF009688, icon = "noodle")
)

@Database(
    entities = [RecipeEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getDatabase(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "yummy_recipes_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 数据库首次创建时，插入预设分类（onCreate 本身已在事务中）
                            DEFAULT_CATEGORIES.forEach { cat ->
                                db.execSQL(
                                    "INSERT INTO categories (name, color, icon) VALUES (?, ?, ?)",
                                    arrayOf(cat.name, cat.color, cat.icon)
                                )
                            }
                        }
                    })
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

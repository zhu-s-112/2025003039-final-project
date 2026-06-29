package com.example.yummyrecipes.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 顶层扩展属性，全局单例 DataStore
val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    /** 深色模式偏好流 */
    val isDarkMode: Flow<Boolean> = context.userPreferencesDataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: false
    }

    /** 保存深色模式设置 */
    suspend fun setDarkMode(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
        }
    }
}

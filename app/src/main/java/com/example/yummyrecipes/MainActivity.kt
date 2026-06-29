package com.example.yummyrecipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.yummyrecipes.data.database.RecipeDatabase
import com.example.yummyrecipes.data.preferences.UserPreferences
import com.example.yummyrecipes.data.repository.RecipeRepository
import com.example.yummyrecipes.navigation.AppNavigation
import com.example.yummyrecipes.ui.theme.YummyRecipesTheme
import com.example.yummyrecipes.viewmodel.RecipeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = RecipeDatabase.getDatabase(applicationContext)
        val repository = RecipeRepository(database.recipeDao(), database.categoryDao())
        val userPreferences = UserPreferences(applicationContext)
        val viewModel = ViewModelProvider(
            this,
            RecipeViewModel.Factory(repository, userPreferences, applicationContext)
        )[RecipeViewModel::class.java]

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            YummyRecipesTheme(darkTheme = uiState.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

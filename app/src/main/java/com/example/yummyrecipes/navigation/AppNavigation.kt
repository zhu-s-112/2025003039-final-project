package com.example.yummyrecipes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.yummyrecipes.ui.screens.*
import com.example.yummyrecipes.viewmodel.RecipeViewModel

@Composable
fun AppNavigation(viewModel: RecipeViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "recipes") {
        // 食谱列表主页
        composable("recipes") {
            RecipeListScreen(
                viewModel = viewModel,
                onNavigateToDetail = { navController.navigate("detail/$it") },
                onNavigateToAdd = { navController.navigate("add") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToDiscover = { navController.navigate("discover") }
            )
        }

        // 网络发现页
        composable("discover") {
            DiscoverScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 食谱详情
        composable(
            "detail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            RecipeDetailScreen(
                viewModel = viewModel,
                recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate("edit/$it") }
            )
        }

        // 新增食谱
        composable("add") {
            AddEditRecipeScreen(
                viewModel = viewModel,
                recipeId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 编辑食谱
        composable(
            "edit/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            AddEditRecipeScreen(
                viewModel = viewModel,
                recipeId = backStackEntry.arguments?.getLong("recipeId"),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 设置
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

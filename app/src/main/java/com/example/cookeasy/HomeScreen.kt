package com.example.cookeasy

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val recipes = remember { mutableStateOf<List<Recipe>>(emptyList()) }
    val filteredRecipes = remember { mutableStateOf<List<Recipe>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var debouncedSearchQuery by remember { mutableStateOf("") }

    // Build the Room database
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "cookeasy-db"
        ).build()
    }

    // Debounce search query to avoid rapid API calls
    LaunchedEffect(searchQuery) {
        delay(500)
        debouncedSearchQuery = searchQuery
    }

    // Fetch recipes from Spoonacular API based on category and debounced search query
    LaunchedEffect(selectedCategory, debouncedSearchQuery) {
        withContext(Dispatchers.IO) {
            try {
                isLoading.value = true
                errorMessage.value = null
                val categoryTag = if (selectedCategory == "All") null else selectedCategory.lowercase()
                val response = if (debouncedSearchQuery.isNotEmpty()) {
                    RetrofitClient.apiService.searchRecipes(
                        query = debouncedSearchQuery,
                        tags = categoryTag,
                        number = 20
                    )
                } else {
                    RetrofitClient.apiService.getRecipes(
                        number = 20,
                        tags = categoryTag
                    )
                }
                val fetchedRecipes = if (debouncedSearchQuery.isNotEmpty()) {
                    response.results ?: emptyList()
                } else {
                    response.recipes ?: emptyList()
                }
                recipes.value = fetchedRecipes
                filteredRecipes.value = fetchedRecipes
                isLoading.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage.value = "Failed to fetch recipes: ${e.message}"
                isLoading.value = false
            }
        }
    }

    // Store saved recipe ids
    val savedRecipeIds = remember { mutableStateOf(emptySet<Int>()) }

    // Load saved recipe ids
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val savedIds = db.recipeDao().getAllIds().toSet()
            savedRecipeIds.value = savedIds
        }
    }

    // Pass state and callbacks to the UI composable
    HomeScreenUI(
        isLoading = isLoading.value,
        filteredRecipes = filteredRecipes.value,
        isSearchExpanded = isSearchExpanded,
        searchQuery = searchQuery,
        onSearchQueryChange = { newQuery -> searchQuery = newQuery },
        onSearchToggle = { isSearchExpanded = !isSearchExpanded },
        selectedCategory = selectedCategory,
        onCategorySelected = { category -> selectedCategory = category },
        savedRecipeIds = savedRecipeIds.value,
        onSaveClick = { recipe, isSaved ->
            coroutineScope.launch(Dispatchers.IO) {
                if (isSaved) {
                    db.recipeDao().deleteById(recipe.id)
                    savedRecipeIds.value = savedRecipeIds.value - recipe.id
                } else {
                    db.recipeDao().insert(
                        RecipeEntity(
                            id = recipe.id,
                            title = recipe.title,
                            summary = recipe.summary,
                            image = recipe.image
                        )
                    )
                    savedRecipeIds.value = savedRecipeIds.value + recipe.id
                }
            }
        },
        onRecipeClick = { recipeId -> navController.navigate("recipeDetail/$recipeId") }
    )

    // Show error message if API call fails
    errorMessage.value?.let { message ->
        LaunchedEffect(Unit) {
            println("Error: $message")
        }
    }
}
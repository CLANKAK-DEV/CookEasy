package com.example.cookeasy

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
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
    val isLoadingMore = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }

    // Expanded list of categories for "All"
    val allCategories = remember {
        listOf(
            "Chicken", "Beef", "Dessert", "Seafood", "Vegetarian",
            "Pasta", "Pork", "Breakfast", "Side", "Soup",
            "Salad", "Lamb", "Miscellaneous", "Starter"
        )
    }
    var currentCategoryIndex by remember { mutableStateOf(0) }

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "cookeasy-db"
        ).build()
    }

    // Debounce search query
    LaunchedEffect(searchQuery) {
        delay(500)
        debouncedSearchQuery = searchQuery
    }

    // Initial fetch and category/search updates
    LaunchedEffect(selectedCategory, debouncedSearchQuery) {
        withContext(Dispatchers.IO) {
            try {
                isLoading.value = true
                errorMessage.value = null
                currentCategoryIndex = 0 // Reset index on new fetch
                recipes.value = emptyList() // Clear previous recipes

                val fetchedRecipes = if (debouncedSearchQuery.isNotEmpty()) {
                    // Search takes priority
                    RetrofitClient.apiService.searchRecipes(debouncedSearchQuery).meals?.map { it.toRecipe() } ?: emptyList()
                } else if (selectedCategory == "All") {
                    // Fetch from the first category initially
                    RetrofitClient.apiService.filterByCategory(allCategories[0]).meals?.map { it.toRecipe() } ?: emptyList()
                } else {
                    // Fetch for selected category
                    RetrofitClient.apiService.filterByCategory(selectedCategory).meals?.map { it.toRecipe() } ?: emptyList()
                }

                recipes.value = fetchedRecipes
                applyFilters(recipes.value, selectedCategory, debouncedSearchQuery, filteredRecipes)
                isLoading.value = false

                if (filteredRecipes.value.isEmpty()) {
                    errorMessage.value = "No recipes found."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage.value = "Failed to fetch recipes: ${e.message}"
                isLoading.value = false
            }
        }
    }

    // Load more recipes when scrolling (only for "All")
    fun loadMoreRecipes() {
        if (selectedCategory != "All" || debouncedSearchQuery.isNotEmpty() || currentCategoryIndex >= allCategories.size || isLoadingMore.value) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                isLoadingMore.value = true
                val nextCategory = allCategories[currentCategoryIndex]
                val moreRecipes = RetrofitClient.apiService.filterByCategory(nextCategory).meals?.map { it.toRecipe() } ?: emptyList()

                recipes.value = recipes.value + moreRecipes // Append new recipes
                applyFilters(recipes.value, selectedCategory, debouncedSearchQuery, filteredRecipes)
                currentCategoryIndex++ // Move to next category
                isLoadingMore.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage.value = "Failed to load more recipes: ${e.message}"
                isLoadingMore.value = false
            }
        }
    }

    // Saved recipes
    val savedRecipeIds = remember { mutableStateOf(emptySet<Int>()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val savedIds = db.recipeDao().getAllIds().toSet()
            savedRecipeIds.value = savedIds
        }
    }

    HomeScreenUI(
        isLoading = isLoading.value,
        isLoadingMore = isLoadingMore.value,
        filteredRecipes = filteredRecipes.value,
        searchQuery = searchQuery,
        onSearchQueryChange = { newQuery -> searchQuery = newQuery },
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
        onRecipeClick = { recipeId -> navController.navigate("recipeDetail/$recipeId") },
        errorMessage = errorMessage.value,
        onLoadMore = { loadMoreRecipes() }
    )
}

// Helper function to apply filters
private fun applyFilters(
    recipes: List<Recipe>,
    selectedCategory: String,
    debouncedSearchQuery: String,
    filteredRecipes: MutableState<List<Recipe>>
) {
    val categoryTag = if (selectedCategory == "All") null else selectedCategory.lowercase()
    filteredRecipes.value = recipes.filter { recipe ->
        (categoryTag == null || recipe.title.lowercase().contains(categoryTag)) &&
                (debouncedSearchQuery.isEmpty() || recipe.title.lowercase().contains(debouncedSearchQuery.lowercase()))
    }
}
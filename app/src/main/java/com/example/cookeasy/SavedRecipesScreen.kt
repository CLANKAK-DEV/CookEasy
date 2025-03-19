package com.example.cookeasy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import coil.compose.AsyncImage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SavedRecipesScreen() {
    val savedRecipes = remember { mutableStateOf<List<RecipeEntity>>(emptyList()) }
    val context = LocalContext.current

    // Build the Room database
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "cookeasy-db"
        ).build()
    }

    // Fetch saved recipes from Room
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            savedRecipes.value = db.recipeDao().getAllSavedRecipes()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(savedRecipes.value) { recipeEntity ->
            SavedRecipeCard(recipe = recipeEntity)
        }
    }
}

@Composable
fun SavedRecipeCard(recipe: RecipeEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Display the recipe image if available
            recipe.image?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Recipe Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }
    }
}
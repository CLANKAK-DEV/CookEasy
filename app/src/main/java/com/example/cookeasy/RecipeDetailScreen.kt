package com.example.cookeasy

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

// Define the color scheme
val Orange = Color(0xFFFF5722)
val White = Color.White
val Black = Color.Black
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: Int
) {
    var recipeDetails by remember { mutableStateOf<TheMealDBMeal?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Fetch recipe details from TheMealDB
    LaunchedEffect(recipeId) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("RecipeDetailScreen", "Fetching recipe details for ID: $recipeId")
                val response = RetrofitClient.apiService.getRecipeDetails(recipeId.toString())
                recipeDetails = response.meals?.firstOrNull()
                Log.d("RecipeDetailScreen", "Successfully fetched recipe details: ${recipeDetails?.strMeal}")
                Log.d("RecipeDetailScreen", "YouTube URL: ${recipeDetails?.strYoutube}")
                isLoading = false
            } catch (e: Exception) {
                Log.e("RecipeDetailScreen", "Failed to load recipe details: ${e.message}", e)
                errorMessage = "Failed to load recipe details: ${e.message}"
                isLoading = false
            }
        }
    }

    // Use WindowInsets to handle system bars
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Recipe Details",
                        color = Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share functionality */ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Recipe",
                            tint = Orange
                        )
                    }
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Orange else Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black,
                    actionIconContentColor = Black,
                    navigationIconContentColor = Black
                ),
                modifier = Modifier
                    .background(White)
                    .windowInsetsPadding(WindowInsets.statusBars) // Extend into status bar
            )
        },
        containerColor = White,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars) // Handle navigation bar
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Orange,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Loading recipe...",
                        color = Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_dialog_alert),
                        contentDescription = "Error",
                        tint = Orange,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { isLoading = true; errorMessage = null; /* Retry logic */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(40.dp)
                    ) {
                        Text(
                            "Retry",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            recipeDetails?.let { details ->
                // Calculate stats using the AI-like function with the meal object
                val stats = suggestRecipeStats(details)
                val ingredients = details.toIngredients()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(White)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        // 1. Recipe Image
                        details.strMealThumb?.let { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Recipe Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 2. Recipe Title with Category
                        Text(
                            text = details.strMeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Black
                        )
                        details.strCategory?.let { category ->
                            Text(
                                text = category,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Orange
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // 3. Recipe Info Cards (Cook Time, Servings, Calories)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RecipeInfoCard(
                                icon = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_recent_history),
                                title = "${stats.cookTime} min",
                                subtitle = "Cook Time"
                            )
                            RecipeInfoCard(
                                icon = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_myplaces),
                                title = stats.servings.toString(),
                                subtitle = "Servings"
                            )
                            RecipeInfoCard(
                                icon = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                                title = stats.calories.toString(),
                                subtitle = "Calories"
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // 4. Watch Preparation Video
                        details.strYoutube?.let { youtubeUrl ->
                            SectionTitle(title = "Watch Preparation Video")
                            Spacer(modifier = Modifier.height(4.dp))

                            val youtubeVideoId = extractYouTubeVideoId(youtubeUrl)
                            if (youtubeVideoId != null) {
                                AndroidView(
                                    factory = {
                                        YouTubePlayerView(context).apply {
                                            lifecycleOwner.lifecycle.addObserver(this)
                                            enableAutomaticInitialization = false
                                            initialize(object : AbstractYouTubePlayerListener() {
                                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                                    youTubePlayer.cueVideo(youtubeVideoId, 0f)
                                                }
                                            })
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                EmptyStateMessage(message = "Invalid YouTube URL.")
                            }
                        } ?: run {
                            EmptyStateMessage(message = "No video available for this recipe.")
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // 5. Ingredients Section
                        SectionTitle(title = "Ingredients")
                        Spacer(modifier = Modifier.height(4.dp))
                        if (ingredients.isEmpty()) {
                            EmptyStateMessage(message = "No ingredients available.")
                        } else {
                            val chunkedIngredients = ingredients.chunked(1)
                            chunkedIngredients.forEach { rowIngredients ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    rowIngredients.forEach { ingredient ->
                                        IngredientCard(
                                            ingredient = ingredient,
                                            modifier = Modifier.fillMaxWidth(0.9f)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // 6. Preparation Steps
                        SectionTitle(title = "Preparation Steps")
                        Spacer(modifier = Modifier.height(4.dp))
                        if (details.strInstructions.isNullOrEmpty()) {
                            EmptyStateMessage(message = "No instructions available.")
                        } else {
                            val steps = details.strInstructions
                                .split(Regex("\\.\\s+|\n+"))
                                .filter { it.isNotBlank() }

                            steps.forEachIndexed { index, instruction ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        color = Orange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$instruction.",
                                        fontSize = 16.sp,
                                        color = Black,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = Black
    )
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Black.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun RecipeInfoCard(
    icon: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = icon,
            contentDescription = subtitle,
            tint = Orange,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Black
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 8.sp,
            color = Black.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun IngredientCard(
    ingredient: Ingredient,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ingredient image
        val ingredientImageUrl = "https://www.themealdb.com/images/ingredients/${ingredient.name}.png"
        AsyncImage(
            model = ingredientImageUrl,
            contentDescription = ingredient.name,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Fit,
            error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
            placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Ingredient details
        Column {
            Text(
                text = ingredient.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatQuantity(ingredient),
                fontSize = 12.sp,
                color = Orange,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun formatQuantity(ingredient: Ingredient): String {
    val amount = ingredient.amount
    val unit = ingredient.unit?.takeIf { it.isNotBlank() } ?: "pieces"
    val formattedAmount = if (amount % 1.0 == 0.0) {
        amount.toInt().toString()
    } else {
        amount.toString()
    }
    return "$formattedAmount $unit"
}

fun extractYouTubeVideoId(url: String): String? {
    val patterns = listOf(
        "(?<=watch\\?v=)[^&]+",
        "(?<=youtu.be/)[^?]+",
        "(?<=embed/)[^?]+"
    )
    for (pattern in patterns) {
        val regex = Regex(pattern)
        val match = regex.find(url)
        if (match != null) {
            return match.value
        }
    }
    return null
}



fun suggestRecipeStats(meal: TheMealDBMeal): RecipeStats {
    // Get the ingredients directly from the meal object
    val ingredients = meal.toIngredients()

    if (ingredients.isEmpty()) {
        return RecipeStats(
            cookTime = 10, // Default for empty list
            servings = 1,
            calories = 100
        )
    }

    // Step 1: Analyze each ingredient
    var totalCookTime = 0
    var totalCalories = 0
    var totalServingContribution = 0.0

    for (ingredient in ingredients) {
        // Infer the type of ingredient based on its name
        val nameLower = ingredient.name.lowercase()
        val isMeat = nameLower.contains("chicken") || nameLower.contains("beef") ||
                nameLower.contains("pork") || nameLower.contains("lamb") || nameLower.contains("fish")
        val isVegetable = nameLower.contains("onion") || nameLower.contains("challot") ||
                nameLower.contains("garlic") || nameLower.contains("carrot") || nameLower.contains("potato")
        val isSpice = nameLower.contains("salt") || nameLower.contains("pepper") || nameLower.contains("spice")
        val isOil = nameLower.contains("oil")

        // Determine base properties based on type
        val baseCookTime = when {
            isMeat -> 20 // Meats take longer to cook
            isVegetable -> 5 // Vegetables are quicker
            isOil || isSpice -> 0 // Spices and oils don't add to cook time
            else -> 5 // Default for unknown ingredients
        }
        val caloriesPerUnit = when {
            isMeat -> 200 // Meats have higher calories
            isVegetable -> 40 // Vegetables have lower calories
            isOil -> 120 // Oils are calorie-dense
            isSpice -> 0 // Spices have negligible calories
            else -> 100 // Default for unknown ingredients
        }
        val servingFactor = when {
            isMeat -> 1.0 // Main ingredients contribute more to servings
            isVegetable -> 0.3 // Vegetables contribute less
            isOil || isSpice -> 0.1 // Oils and spices contribute minimally
            else -> 0.3 // Default for unknown ingredients
        }

        // Adjust cook time based on quantity
        val cookTimeForIngredient = baseCookTime + (ingredient.amount * 2).toInt() // Add 2 minutes per unit
        totalCookTime = maxOf(totalCookTime, cookTimeForIngredient)

        // Calculate calories based on quantity
        totalCalories += (caloriesPerUnit * ingredient.amount).toInt()

        // Calculate serving contribution based on quantity
        totalServingContribution += servingFactor * ingredient.amount
    }

    // Step 2: Incorporate YouTube video duration (if available)
    val youtubeDuration = meal.strYoutube?.let { extractVideoDuration(it) }
    if (youtubeDuration != null) {
        // Use the YouTube duration as a baseline, but ensure it's at least as long as the ingredient-based cook time
        totalCookTime = maxOf(totalCookTime, youtubeDuration)
        // Add preparation time based on the number of ingredients
        totalCookTime += ingredients.size * 2 // Add 2 minutes per ingredient for prep
    } else {
        // If no YouTube duration, add preparation time and apply other adjustments
        totalCookTime += ingredients.size * 2 // Add 2 minutes per ingredient for prep
        // If there's a main ingredient (e.g., meat), increase cook time slightly
        val hasMainIngredient = ingredients.any { ingredient ->
            val nameLower = ingredient.name.lowercase()
            nameLower.contains("chicken") || nameLower.contains("beef") ||
                    nameLower.contains("pork") || nameLower.contains("lamb") || nameLower.contains("fish")
        }
        if (hasMainIngredient) {
            totalCookTime += 5 // Add 5 minutes for main ingredients
        }
        // If there are many ingredients, increase cook time
        if (ingredients.size > 5) {
            totalCookTime += 10 // Add 10 minutes for complex recipes
        }
    }
    totalCookTime = totalCookTime.coerceIn(10, 120) // Cap between 10 and 120 minutes

    // Step 3: Adjust servings based on total contribution
    val servings = maxOf(1, (totalServingContribution / 2.0).toInt()) // 1 serving per 2 units of contribution
    // Adjust servings based on the number of ingredients (more ingredients might mean a larger recipe)
    val adjustedServings = if (ingredients.size > 5) servings + 1 else servings
    adjustedServings.coerceIn(1, 12) // Cap between 1 and 12 servings

    // Step 4: Adjust calories based on the number of ingredients
    totalCalories = totalCalories.coerceIn(100, 2000) // Cap between 100 and 2000 kcal
    // If there are many ingredients, assume some additional calories for preparation (e.g., oil, seasoning)
    if (ingredients.size > 5) {
        totalCalories += 50 // Add 50 kcal for complex recipes
    }

    // Step 5: Apply contextual rules based on recipe category (if available)
    meal.strCategory?.let { category ->
        if (category.lowercase() == "dessert") {
            totalCookTime += 5 // Desserts might need extra baking time
            totalCalories += 100 // Desserts often have higher calories
        } else if (category.lowercase() == "salad") {
            totalCookTime = (totalCookTime * 0.8).toInt() // Salads are quicker to prepare
            totalCalories = (totalCalories * 0.7).toInt() // Salads have fewer calories
        }
    }

    return RecipeStats(
        cookTime = totalCookTime,
        servings = adjustedServings,
        calories = totalCalories
    )
}
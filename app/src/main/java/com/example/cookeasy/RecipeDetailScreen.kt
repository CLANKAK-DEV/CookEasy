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

    // Generate placeholder stats for UI enhancement
    val cookTime = remember { Random.nextInt(10, 60) }
    val servings = remember { Random.nextInt(2, 8) }
    val calories = remember { Random.nextInt(250, 800) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Recipe Details",
                        color = Black,
                        fontSize = 20.sp, // Simplified font size
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
                )
            )
        },
        containerColor = White
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
                        modifier = Modifier.size(40.dp) // Simplified size
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing
                    Text(
                        "Loading recipe...",
                        color = Black,
                        fontSize = 16.sp, // Simplified font size
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
                        modifier = Modifier.size(48.dp) // Simplified size
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = Black,
                        fontSize = 16.sp, // Simplified font size
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp)) // Reduced spacing
                    Button(
                        onClick = { isLoading = true; errorMessage = null; /* Retry logic */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(8.dp), // Simplified shape
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(40.dp) // Simplified height
                    ) {
                        Text(
                            "Retry",
                            fontSize = 14.sp, // Simplified font size
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            recipeDetails?.let { details ->
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
                                    .height(200.dp) // Simplified height
                                    .clip(RoundedCornerShape(8.dp)), // Simplified corner radius
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing
                        }

                        // 2. Recipe Title with Category
                        Text(
                            text = details.strMeal,
                            fontWeight = FontWeight.Bold, // Simplified weight
                            fontSize = 28.sp, // Slightly reduced font size
                            color = Black
                        )
                        details.strCategory?.let { category ->
                            Text(
                                text = category,
                                fontSize = 16.sp, // Simplified font size
                                fontWeight = FontWeight.Medium,
                                color = Orange
                            )
                            Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing
                        }

                        // 3. Recipe Info Cards (Cook Time, Servings, Calories)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RecipeInfoCard(
                                icon = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_recent_history),
                                title = "$cookTime min",
                                subtitle = "Cook Time"
                            )
                            RecipeInfoCard(
                                icon = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_myplaces),
                                title = "$servings",
                                subtitle = "Servings"
                            )
                            RecipeInfoCard(
                                icon = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                                title = "$calories",
                                subtitle = "Calories"
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

                        // 4. Watch Preparation Video
                        details.strYoutube?.let { youtubeUrl ->
                            SectionTitle(title = "Watch Preparation Video")
                            Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing

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
                                        .height(200.dp) // Simplified height
                                        .clip(RoundedCornerShape(8.dp)) // Simplified corner radius
                                )
                            } else {
                                EmptyStateMessage(message = "Invalid YouTube URL.")
                            }
                        } ?: run {
                            EmptyStateMessage(message = "No video available for this recipe.")
                        }
                        Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

                        // 5. Ingredients Section
                        SectionTitle(title = "Ingredients")
                        Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing
                        val ingredients = details.toIngredients()
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
                        Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

                        // 6. Preparation Steps
                        SectionTitle(title = "Preparation Steps")
                        Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing
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
                                        .padding(vertical = 4.dp), // Reduced padding
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        color = Orange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp)) // Reduced spacing
                                    Text(
                                        text = "$instruction.",
                                        fontSize = 16.sp,
                                        color = Black,
                                        lineHeight = 22.sp, // Simplified line height
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp)) // Reduced final spacing
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
        fontSize = 20.sp, // Simplified font size
        color = Black
    )
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Reduced padding
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp, // Simplified font size
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
            .padding(horizontal = 4.dp), // Simplified padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = icon,
            contentDescription = subtitle,
            tint = Orange,
            modifier = Modifier.size(20.dp) // Simplified size
        )
        Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp, // Simplified font size
            color = Black
        )
        Spacer(modifier = Modifier.height(2.dp)) // Reduced spacing
        Text(
            text = subtitle,
            fontSize = 8.sp, // Simplified font size
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
            .padding(vertical = 4.dp), // Reduced padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ingredient image
        val ingredientImageUrl = "https://www.themealdb.com/images/ingredients/${ingredient.name}.png"
        AsyncImage(
            model = ingredientImageUrl,
            contentDescription = ingredient.name,
            modifier = Modifier
                .size(48.dp) // Simplified size
                .clip(RoundedCornerShape(4.dp)), // Simplified corner radius
            contentScale = ContentScale.Fit,
            error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
            placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
        )
        Spacer(modifier = Modifier.width(8.dp)) // Reduced spacing
        // Ingredient details
        Column {
            Text(
                text = ingredient.name,
                fontWeight = FontWeight.Bold, // Simplified weight
                fontSize = 16.sp, // Simplified font size
                color = Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp)) // Reduced spacing
            Text(
                text = formatQuantity(ingredient),
                fontSize = 12.sp, // Simplified font size
                color = Orange,
                fontWeight = FontWeight.Medium, // Simplified weight
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
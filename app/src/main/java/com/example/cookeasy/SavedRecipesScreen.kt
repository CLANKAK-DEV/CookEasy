package com.example.cookeasy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.room.Room
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRecipesScreen(navController: NavHostController) {
    val savedRecipes = remember { mutableStateOf<List<RecipeEntity>>(emptyList()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Color palette
    val primaryColor = Color(0xFFFF5722)
    val backgroundColor = Color.White
    val textColor = Color.Black

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

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved Recipes",
                        color = textColor,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        if (savedRecipes.value.isEmpty()) {
            // Show empty state card when there are no saved recipes
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {

            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = savedRecipes.value,
                    key = { it.id }
                ) { recipeEntity ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        SavedRecipeCard(
                            recipe = recipeEntity,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            onDelete = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        db.recipeDao().deleteRecipe(recipeEntity)
                                        savedRecipes.value = db.recipeDao().getAllSavedRecipes()
                                    }
                                }
                            },
                            onClick = {
                                navController.navigate("recipeDetail/${recipeEntity.id}") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedRecipeCard(
    recipe: RecipeEntity,
    primaryColor: Color,
    textColor: Color,
    onDelete: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    // Determine if this is an empty state card
    val isEmptyState = recipe.title == "No Saved Recipes" && recipe.image == null

    // Outer Box with primaryColor border/background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)  // Space for the border
            .background(
                color = primaryColor.copy(alpha = 0.1f),  // Subtle background with primaryColor
                shape = RoundedCornerShape(20.dp)
            )
            .padding(4.dp)  // Inner padding to create border effect
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isEmptyState) {
                    // Empty State Content
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Recipe",
                        tint = primaryColor,
                        modifier = Modifier
                            .size(48.dp)
                            .background(primaryColor.copy(alpha = 0.1f), shape = CircleShape)
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You haven't saved any recipes yet. Start adding some delicious recipes to your collection!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Add Recipes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Regular Recipe Card Content
                    recipe.image?.let { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Recipe Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tap to view details",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.Transparent, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Recipe",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

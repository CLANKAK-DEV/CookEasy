package com.example.cookeasy

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cookeasy.Recipe

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenUI(
    isLoading: Boolean,
    filteredRecipes: List<Recipe>,
    isSearchExpanded: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    savedRecipeIds: Set<Int>,
    onSaveClick: (Recipe, Boolean) -> Unit,
    onRecipeClick: (Int) -> Unit // Add callback for recipe card click
) {
    val categories = listOf("All", "Breakfast", "Lunch", "Dinner", "Dessert", "Snacks")
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            // Top App Bar with "CookEasy" and animated Search Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Animated "CookEasy" text
                val textAlpha by animateFloatAsState(
                    targetValue = if (isSearchExpanded) 0.3f else 1f,
                    animationSpec = tween(durationMillis = 300),
                    label = "Text Alpha Animation"
                )
                val textScale by animateFloatAsState(
                    targetValue = if (isSearchExpanded) 0.9f else 1f,
                    animationSpec = tween(durationMillis = 300),
                    label = "Text Scale Animation"
                )

                Text(
                    text = "CookEasy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .graphicsLayer(
                            alpha = textAlpha,
                            scaleX = textScale,
                            scaleY = textScale
                        )
                )

                // Search icon with animation
                val searchWidthAnimation by animateDpAsState(
                    targetValue = if (isSearchExpanded) 240.dp else 48.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "Search Width Animation"
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(searchWidthAnimation)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isSearchExpanded) Color(0xFFF5F5F5) else Color.Transparent)
                        .clickable { onSearchToggle() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    AnimatedVisibility(
                        visible = isSearchExpanded,
                        enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End),
                        modifier = Modifier.weight(1f)
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 8.dp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search recipes...",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                        )
                    }

                    // Search icon with ripple and rotation animation
                    val rotationAnimation by animateFloatAsState(
                        targetValue = if (isSearchExpanded) 90f else 0f,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        ),
                        label = "Rotation Animation"
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSearchExpanded) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchExpanded) "Close Search" else "Search",
                            tint = if (isSearchExpanded) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.graphicsLayer { rotationZ = rotationAnimation }
                        )
                    }
                }
            }

            // Categories Row
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LazyRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            name = category,
                            isSelected = category == selectedCategory,
                            onSelected = { onCategorySelected(category) }
                        )
                    }
                }
            }

            // Recipe List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF5722),
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    itemsIndexed(filteredRecipes) { index, recipe ->
                        val isSaved = savedRecipeIds.contains(recipe.id)
                        val animatedModifier = Modifier
                            .animateItemPlacement(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                            .animateContentSize()

                        EnhancedRecipeCard(
                            recipe = recipe,
                            isSaved = isSaved,
                            onSaveClick = { onSaveClick(recipe, isSaved) },
                            onCardClick = { onRecipeClick(recipe.id) }, // Trigger navigation on card click
                            modifier = animatedModifier,
                            index = index
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(name: String, isSelected: Boolean, onSelected: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFFFF5722) else Color(0xFFF5F5F5)
    val textColor = if (isSelected) Color.White else Color.Gray

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable { onSelected() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = name,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun EnhancedRecipeCard(
    recipe: Recipe,
    isSaved: Boolean,
    onSaveClick: () -> Unit,
    onCardClick: () -> Unit, // Add callback for card click
    modifier: Modifier = Modifier,
    index: Int
) {
    val animationDelay = 100 * index
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) + expandVertically(
            animationSpec = tween(durationMillis = 300)
        ),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0x1A000000)
                )
                .clickable { onCardClick() }, // Trigger navigation on card click
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        recipe.image?.let { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Recipe Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            ),
                                            startY = 0f,
                                            endY = Float.POSITIVE_INFINITY
                                        )
                                    )
                            )

                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = recipe.summary.take(120) + "...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RecipeTag(text = "30 min")
                        Spacer(modifier = Modifier.width(8.dp))
                        RecipeTag(text = "Easy")

                        Spacer(modifier = Modifier.weight(1f))

                        SaveButton(isSaved = isSaved, onClick = onSaveClick)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun RecipeTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFEEEEEE))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.DarkGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SaveButton(isSaved: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (isSaved) 360f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    val buttonColor = if (isSaved) Color(0xFFFF5722) else Color.LightGray

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .graphicsLayer { rotationZ = rotation }
            .shadow(2.dp, CircleShape)
            .background(buttonColor, CircleShape)
    ) {
        Icon(
            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isSaved) "Saved" else "Save",
            tint = Color.White
        )
    }
}
package com.example.cookeasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.cookeasy.ui.theme.CookEasyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CookEasyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = { EnhancedBottomNavigationBar(navController) }
                        ) { innerPadding ->
                            SetupNavGraph(
                                navController = navController,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetupNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") { HomeScreen(navController) }
        composable("saved") { SavedRecipesScreen() }
        composable("tips") { TipsScreen() }
        composable("settings") { SettingsScreen() }
        composable("recipeDetail/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull() ?: 0
            RecipeDetailScreen(navController = navController, recipeId = recipeId)
        }
    }
}

data class BottomNavItem(
    val route: String,
    val iconRes: Int,
    val label: String
)

@Composable
fun EnhancedBottomNavigationBar(navController: NavHostController) {
    val navItems = listOf(
        BottomNavItem("home", R.drawable.home, "Home"),
        BottomNavItem("saved", R.drawable.save, "Saved"),
        BottomNavItem("tips", R.drawable.idea, "Tips"),
        BottomNavItem("settings",R.drawable.setting, "Settings")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isBarVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isBarVisible = true
    }

    AnimatedVisibility(
        visible = isBarVisible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 500),
            initialOffsetY = { it }
        ) + fadeIn(animationSpec = tween(durationMillis = 500)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        spotColor = Color(0x1A000000)
                    ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentRoute == item.route

                        var isClicked by remember { mutableStateOf(false) }
                        LaunchedEffect(isSelected) {
                            if (isSelected) isClicked = true
                        }

                        val scale by animateFloatAsState(
                            targetValue = if (isClicked) 1.2f else if (isSelected) 1.1f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "Scale Animation"
                        )

                        val containerAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            animationSpec = tween(durationMillis = 200),
                            label = "Alpha Animation"
                        )

                        LaunchedEffect(isClicked) {
                            if (isClicked) {
                                kotlinx.coroutines.delay(200)
                                isClicked = false
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                                .padding(3.dp)
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(42.dp)
                                        .graphicsLayer(alpha = containerAlpha)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF5722).copy(alpha = 0.2f))
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .matchParentSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    )
                                    .clickable {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.startDestinationId)
                                                launchSingleTop = true
                                            }
                                            isClicked = true
                                        }
                                    }
                                    .padding(3.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.label,
                                    tint = if (isSelected) Color(0xFFFF5722) else Color.Gray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = item.label,
                                    color = if (isSelected) Color(0xFFFF5722) else Color.Gray.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun windowWidth(): Int {
    return LocalContext.current.resources.displayMetrics.widthPixels /
            LocalContext.current.resources.displayMetrics.density.toInt()
}


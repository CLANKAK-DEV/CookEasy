package com.example.cookeasy

import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.delay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.cookeasy.ui.theme.CookEasyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.Transparent.toArgb(),
                darkScrim = Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.Transparent.toArgb(),
                darkScrim = Color.Transparent.toArgb()
            )
        )

        // Initialize Room database
        val database by lazy {
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "cookeasy_db")
                .fallbackToDestructiveMigration() // For development only; remove in production
                .build()
        }
        val recipeDao by lazy { database.recipeDao() }

        setContent {
            CookEasyTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = { EnhancedBottomNavigationBar(navController) }
                    ) { innerPadding ->
                        SetupNavGraph(
                            navController = navController,
                            recipeDao = recipeDao,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    recipeDao: RecipeDao,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") { HomeScreen(navController) }
        composable("saved") { SavedRecipesScreen(navController) }
        composable("tips") { TipsScreen() }
        composable("settings") {
            SettingsScreen(recipeDao = recipeDao) {
                navController.popBackStack()
            }
        }
        composable(
            "recipeDetail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId")
            if (recipeId == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                return@composable
            }
            RecipeDetailScreen(navController, recipeId)
        }
    }
}




@Composable
fun EnhancedBottomNavigationBar(navController: NavHostController) {
    val navItems = listOf(
        BottomNavItem("home", R.drawable.home, "Home"),
        BottomNavItem("saved", R.drawable.save, "Saved"),
        BottomNavItem("tips", R.drawable.idea, "Tips"),
        BottomNavItem("settings", R.drawable.setting, "Settings")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var isBarVisible by remember { mutableStateOf(false) }

    // Animate the bottom bar entrance
    LaunchedEffect(Unit) {
        isBarVisible = true
    }

    AnimatedVisibility(
        visible = isBarVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(animationSpec = tween(500)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    clip = true
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

                    // Trigger click animation when selected
                    LaunchedEffect(isSelected) {
                        if (isSelected) isClicked = true
                    }

                    // Scale animation for the entire item (icon + text)
                    val scale by animateFloatAsState(
                        targetValue = if (isClicked) 1.3f else if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "Scale Animation"
                    )

                    // Vertical offset animation for icon and text
                    val verticalOffset by animateFloatAsState(
                        targetValue = if (isClicked) -5f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "Vertical Offset Animation"
                    )

                    LaunchedEffect(isClicked) {
                        if (isClicked) {
                            delay(200)
                            isClicked = false
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f, matchHeightConstraintsFirst = true)
                            .padding(3.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .matchParentSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationY = verticalOffset
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = LocalIndication.current
                                ) {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
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
data class BottomNavItem(val route: String, val iconRes: Int, val label: String)

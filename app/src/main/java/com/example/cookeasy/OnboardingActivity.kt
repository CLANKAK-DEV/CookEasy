package com.example.cookeasy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import com.example.cookeasy.ui.theme.CookEasyTheme
import kotlin.math.absoluteValue

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if onboarding is already completed
        val sharedPrefs = getSharedPreferences("CookEasyPrefs", MODE_PRIVATE)
        val onboardingCompleted = sharedPrefs.getBoolean("onboarding_completed", false)

        if (onboardingCompleted) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            CookEasyTheme { // Use CookEasyTheme for consistency with MainActivity
                OnboardingScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun OnboardingScreen() {
        val pages = listOf(
            OnboardingPage(
                "Welcome to CookEasy",
                "Discover delicious recipes and simplify your cooking experience",
                "🍳",
                Color(0xFFFF5722) // Orange theme
            ),
            OnboardingPage(
                "Find Perfect Recipes",
                "Browse through a variety of dishes for any occasion and skill level",
                "📖",
                Color(0xFF4CAF50) // Green theme
            ),
            OnboardingPage(
                "Cook With Confidence",
                "Follow simple step-by-step instructions to create amazing meals",
                "👨‍🍳",
                Color(0xFFE91E63) // Pink theme
            )
        )
        val pagerState = rememberPagerState(pageCount = { pages.size })
        val scope = rememberCoroutineScope()

        // Animation for button bounce effect
        val buttonBounce = remember { Animatable(1f) }

        LaunchedEffect(pagerState.currentPage) {
            // Button bounce animation when page changes
            buttonBounce.snapTo(0.9f)
            buttonBounce.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            pages[pagerState.currentPage].backgroundColor.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            // Animated background wave shape
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer {
                        translationY = 150f
                    }
                    .clip(
                        RoundedCornerShape(
                            topStart = 180.dp,
                            topEnd = 180.dp
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                pages[pagerState.currentPage].backgroundColor.copy(alpha = 0.4f),
                                pages[pagerState.currentPage].backgroundColor.copy(alpha = 0.05f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    OnboardingPageItem(
                        page = pages[page],
                        pageOffset = pageOffset
                    )
                }

                AnimatedPagerIndicator(
                    pagerState = pagerState,
                    activeColor = pages[pagerState.currentPage].backgroundColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            // Since you mentioned not having login, go directly to MainActivity
                            // Store a preference to indicate onboarding is complete
                            val sharedPrefs = getSharedPreferences("CookEasyPrefs", MODE_PRIVATE)
                            sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()

                            Log.d("OnboardingActivity", "Onboarding completed, moving to MainActivity")
                            startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                            finish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].backgroundColor
                    ),
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(buttonBounce.value),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (pagerState.currentPage < pages.size - 1) "Continue" else "Get Started",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White
                        )
                    }
                }

                // Skip button for pages before last
                AnimatedVisibility(
                    visible = pagerState.currentPage < pages.size - 1,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    TextButton(
                        onClick = {
                            // Go directly to the last page
                            scope.launch {
                                pagerState.animateScrollToPage(pages.size - 1)
                            }
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Skip",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun OnboardingPageItem(page: OnboardingPage, pageOffset: Float) {
        // Animations based on page offset
        val scale = lerp(
            start = 0.85f,
            stop = 1f,
            fraction = 1f - pageOffset.coerceIn(0f, 1f)
        )

        val alpha = lerp(
            start = 0.5f,
            stop = 1f,
            fraction = 1f - pageOffset.coerceIn(0f, 1f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emoji icon with pulsating animation
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val iconScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "icon pulse"
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(iconScale)
                    .background(page.backgroundColor.copy(alpha = 0.2f), CircleShape)
                    .border(
                        BorderStroke(width = 2.dp, color = page.backgroundColor.copy(alpha = 0.5f)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = page.emoji,
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = page.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = page.description,
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AnimatedPagerIndicator(pagerState: PagerState, activeColor: Color) {
        Row(
            modifier = Modifier
                .height(24.dp)
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { index ->
                val isSelected = pagerState.currentPage == index

                // Size animation for the indicator
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "indicator width"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) activeColor else Color.LightGray.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }

    data class OnboardingPage(
        val title: String,
        val description: String,
        val emoji: String,
        val backgroundColor: Color
    )

    // Helper function for linear interpolation
    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + fraction * (stop - start)
    }
}
package com.example.cookeasy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookeasy.ui.theme.CookEasyTheme
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import kotlin.math.sin

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookEasyTheme {
                SplashScreen()
            }
        }
    }

    @Composable
    fun SplashScreen() {
        val logoScale = remember { Animatable(0.3f) }
        var showTagline by remember { mutableStateOf(false) }
        var showWaves by remember { mutableStateOf(false) } // Delay wave rendering
        val waveAnimation = rememberInfiniteTransition(label = "wave")
        val waveOffset = waveAnimation.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave motion"
        )

        LaunchedEffect(Unit) {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(1000, easing = EaseOutBack)
            )
            showTagline = true
            showWaves = true // Show waves after initial render
            delay(2500)

            val sharedPrefs = getSharedPreferences("CookEasyPrefs", MODE_PRIVATE)
            val onboardingCompleted = sharedPrefs.getBoolean("onboarding_completed", false)
            val intent = if (onboardingCompleted) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, OnboardingActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF5722), Color(0xFFFF8A65))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = showWaves,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-50).dp)
                ) {
                    for (i in 0..2) {
                        WaveLine(
                            waveOffset = waveOffset.value,
                            amplitude = 25f + (i * 5),
                            phase = i * 500f,
                            alpha = 0.2f - (i * 0.05f)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale.value)
                        .clip(CircleShape)
                        .background(Color(0xFFF5E9D6)), // Updated to the correct beige color
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.appst),
                        contentDescription = "CookEasy Logo",
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "CookEasy",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                AnimatedVisibility(
                    visible = showTagline,
                    enter = fadeIn(animationSpec = tween(500)) +
                            slideInVertically(animationSpec = tween(500)) { it }
                ) {
                    Text(
                        text = "Recipes made simple",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun WaveLine(waveOffset: Float, amplitude: Float, phase: Float, alpha: Float) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height * 0.8f

            val path = Path().apply {
                moveTo(0f, centerY)
                for (x in 0..width.toInt() step 10) {
                    val y = centerY + amplitude * sin((x + waveOffset + phase) * (2f * Math.PI / 800f).toFloat())
                    lineTo(x.toFloat(), y)
                }
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = alpha), Color.White.copy(alpha = 0f)),
                    startY = centerY,
                    endY = centerY + 200f
                )
            )
        }
    }
}
package com.example.cookeasy

// TipsScreen.kt
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TipsScreen() {
    val tips = remember { mutableStateOf<List<Tip>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val tip = RetrofitClient.apiService.getTip()
            tips.value = listOf(tip) // Spoonacular returns one trivia at a time; adjust for multiple if needed
        }
    }

    LazyColumn {
        items(tips.value) { tip ->
            Text(text = tip.content)
        }
    }
}
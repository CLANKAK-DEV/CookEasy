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



    LazyColumn {
        items(tips.value) { tip ->
            Text(text = tip.content)
        }
    }
}
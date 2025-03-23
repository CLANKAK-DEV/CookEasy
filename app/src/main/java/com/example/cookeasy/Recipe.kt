package com.example.cookeasy



data class Recipe(
    val id: Int,
    val title: String,
    val summary: String,
    val image: String?,
    val category: String?,
    val youtubeUrl: String? = null,
    val videoDuration: Int? = null // Add this field for YouTube video duration in seconds
)
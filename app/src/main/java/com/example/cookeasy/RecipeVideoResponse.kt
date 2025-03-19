package com.example.cookeasy

data class RecipeVideoResponse(
    val videos: List<RecipeVideo>?
)

data class RecipeVideo(
    val title: String,
    val youTubeId: String?, // The YouTube video ID (e.g., "dQw4w9WgXcQ")
    val thumbnail: String?, // Thumbnail URL for the video
    val views: Int?,        // Number of views
    val length: Int?        // Length of the video in seconds
)
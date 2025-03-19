package com.example.cookeasy

data class RecipeResponse(
    val results: List<Recipe>? = emptyList(), // For complexSearch
    val recipes: List<Recipe>? = emptyList()  // For random
)
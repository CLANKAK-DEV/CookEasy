package com.example.cookeasy


data class RecipeDetails(
    val id: Int,
    val title: String,
    val image: String?,
    val summary: String,
    val instructions: String?, // Preparation steps
    val sourceUrl: String?,   // URL to the original recipe (might include a video)
    val readyInMinutes: Int,
    val servings: Int,
    val extendedIngredients: List<Ingredient>?
)

data class Ingredient(
    val name: String,
    val amount: Double,
    val unit: String?
)
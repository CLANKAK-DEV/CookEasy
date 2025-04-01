package com.example.cookeasy

import androidx.room.Delete
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("search.php")
    suspend fun searchRecipes(
        @Query("s") query: String? = null
    ): TheMealDBResponse

    @GET("filter.php")
    suspend fun filterByCategory(
        @Query("c") category: String
    ): TheMealDBResponse

    @GET("lookup.php")
    suspend fun getRecipeDetails(
        @Query("i") id: String
    ): TheMealDBResponse
}

data class TheMealDBResponse(
    val meals: List<TheMealDBMeal>?
)

data class TheMealDBMeal(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String?,
    val strCategory: String?,
    val strInstructions: String?,
    val strYoutube: String?,
    val strSource: String?,
    val strIngredient1: String?, val strMeasure1: String?,
    val strIngredient2: String?, val strMeasure2: String?,
    val strIngredient3: String?, val strMeasure3: String?,
    val strIngredient4: String?, val strMeasure4: String?,
    val strIngredient5: String?, val strMeasure5: String?,
    val strIngredient6: String?, val strMeasure6: String?,
    val strIngredient7: String?, val strMeasure7: String?,
    val strIngredient8: String?, val strMeasure8: String?,
    val strIngredient9: String?, val strMeasure9: String?,
    val strIngredient10: String?, val strMeasure10: String?,
    val strIngredient11: String?, val strMeasure11: String?,
    val strIngredient12: String?, val strMeasure12: String?,
    val strIngredient13: String?, val strMeasure13: String?,
    val strIngredient14: String?, val strMeasure14: String?,
    val strIngredient15: String?, val strMeasure15: String?,
    val strIngredient16: String?, val strMeasure16: String?,
    val strIngredient17: String?, val strMeasure17: String?,
    val strIngredient18: String?, val strMeasure18: String?,
    val strIngredient19: String?, val strMeasure19: String?,
    val strIngredient20: String?, val strMeasure20: String?
)



// Updated Recipe conversion - only uses YouTube duration
fun TheMealDBMeal.toRecipe(category: String? = null): Recipe {
    return Recipe(
        id = idMeal.toIntOrNull() ?: 0,
        title = strMeal,
        image = strMealThumb,
        summary = strInstructions ?: "",
        youtubeUrl = strYoutube,
        category = category
    )
}

// Extract duration from YouTube URL
fun extractVideoDuration(youtubeUrl: String): Int? {
    return try {
        val timeParam = youtubeUrl.split("&").find { it.startsWith("t=") }
        timeParam?.removePrefix("t=")?.toIntOrNull()?.let { seconds ->
            seconds / 60 // Convert seconds to minutes
        }
    } catch (e: Exception) {
        null
    }
}

fun TheMealDBMeal.toIngredients(): List<Ingredient> {
    val ingredients = mutableListOf<Ingredient>()
    val ingredientPairs = listOf(
        strIngredient1 to strMeasure1,
        strIngredient2 to strMeasure2,
        strIngredient3 to strMeasure3,
        strIngredient4 to strMeasure4,
        strIngredient5 to strMeasure5,
        strIngredient6 to strMeasure6,
        strIngredient7 to strMeasure7,
        strIngredient8 to strMeasure8,
        strIngredient9 to strMeasure9,
        strIngredient10 to strMeasure10,
        strIngredient11 to strMeasure11,
        strIngredient12 to strMeasure12,
        strIngredient13 to strMeasure13,
        strIngredient14 to strMeasure14,
        strIngredient15 to strMeasure15,
        strIngredient16 to strMeasure16,
        strIngredient17 to strMeasure17,
        strIngredient18 to strMeasure18,
        strIngredient19 to strMeasure19,
        strIngredient20 to strMeasure20
    )

    ingredientPairs.forEach { (name, measure) ->
        if (!name.isNullOrBlank()) {
            ingredients.add(
                Ingredient(
                    name = name,
                    amount = measure?.toDoubleOrNull() ?: 0.0,
                    unit = if (measure?.toDoubleOrNull() == null) measure else null
                )
            )
        }
    }
    return ingredients
}
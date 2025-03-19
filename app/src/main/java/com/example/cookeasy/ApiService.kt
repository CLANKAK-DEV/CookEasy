package com.example.cookeasy



import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("recipes/random")
    suspend fun getRecipes(
        @Query("apiKey") apiKey: String = "dd7e4338dc454c74879d544c94ca3492",
        @Query("number") number: Int = 10,
        @Query("tags") tags: String? = null
    ): RecipeResponse

    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("apiKey") apiKey: String = "dd7e4338dc454c74879d544c94ca3492",
        @Query("query") query: String? = null,
        @Query("tags") tags: String? = null,
        @Query("number") number: Int = 20
    ): RecipeResponse

    @GET("food/trivia/random")
    suspend fun getTip(
        @Query("apiKey") apiKey: String = "dd7e4338dc454c74879d544c94ca3492"
    ): Tip

    @GET("recipes/{id}/information")
    suspend fun getRecipeDetails(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String = "dd7e4338dc454c74879d544c94ca3492",
        @Query("includeNutrition") includeNutrition: Boolean = false
    ): RecipeDetails


    @GET("recipes/{id}/videos")
    suspend fun getRecipeVideos(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String = "dd7e4338dc454c74879d544c94ca3492"
    ): RecipeVideoResponse
}
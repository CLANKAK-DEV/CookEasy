package com.example.cookeasy

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query



@Dao
interface RecipeDao {
    @Insert
    suspend fun insert(recipe: RecipeEntity)
    @Query("DELETE FROM saved_recipes")
    suspend fun deleteAllRecipes() // New method to clear all recipes

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)  // Add delete method

    @Query("SELECT * FROM saved_recipes")
    suspend fun getAllSavedRecipes(): List<RecipeEntity>

    @Query("SELECT id FROM saved_recipes")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM saved_recipes WHERE id = :id")
    suspend fun deleteById(id: Int)

    // New method for getting a single recipe
    @Query("SELECT * FROM saved_recipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): RecipeEntity?
}
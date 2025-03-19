package com.example.cookeasy

// RecipeDao.kt
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeDao {
    @Insert
    suspend fun insert(recipe: RecipeEntity)

    @Query("SELECT * FROM saved_recipes")
    suspend fun getAllSavedRecipes(): List<RecipeEntity>

    @Query("SELECT id FROM saved_recipes")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM saved_recipes WHERE id = :id")
    suspend fun deleteById(id: Int)
}
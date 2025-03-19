package com.example.cookeasy

// RecipeEntity.kt
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_recipes")
data class RecipeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val summary: String,
    val image: String?
)
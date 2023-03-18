package com.olt.mor.common.database.data

data class Recipe(
    val id: Long,
    val name: String,
    val author: String,
    val rating: Int,
    val workTimeInMinutes: Int,
    val cookTimeInMinutes: Int,
    val difficulty: Difficulty,
    val portions: Int,
    val text: String,
    val tags: List<RecipeTag>,
    val ingredients: List<RecipeIngredient>
)
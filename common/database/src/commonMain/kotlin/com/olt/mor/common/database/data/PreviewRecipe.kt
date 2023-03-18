package com.olt.mor.common.database.data

data class PreviewRecipe(
    val id: Long,
    val name: String,
    val author: String,
    val rating: Int,
    val difficulty: Difficulty,
    val time: Int,
    val tags: List<RecipeTag.ExistingTag>
)

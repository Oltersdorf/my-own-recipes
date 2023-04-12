package com.olt.mor.common.api.data

data class RecipePreview(
    val id: Long,
    val name: String,
    val author: String,
    val rating: Int,
    val difficulty: Difficulty,
    val time: Int,
    val tags: List<Tag.Existing>
)
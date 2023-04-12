package com.olt.mor.common.search

sealed class Filter {
    data class Name(val name: String) : Filter()
    data class Author(val author: String) : Filter()
    data class Rating(val rating: Int) : Filter()
    data class MaxTime(val maxTime: Int) : Filter()
    data class Difficulty(val difficulty: com.olt.mor.common.api.data.Difficulty) : Filter()
    data class Tag(val tag: com.olt.mor.common.api.data.Tag.Existing) : Filter()
    data class Ingredient(val ingredient: com.olt.mor.common.api.data.Ingredient.Existing) : Filter()
}
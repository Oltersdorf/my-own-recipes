package com.olt.mor.common.search

import com.olt.mor.common.database.data.RecipeIngredient
import com.olt.mor.common.database.data.RecipeTag

sealed class Filter {
    data class Name(val name: String) : Filter()
    data class Author(val author: String) : Filter()
    data class Rating(val rating: Int) : Filter()
    data class MaxTime(val maxTime: Int) : Filter()
    data class Difficulty(val difficulty: com.olt.mor.common.database.data.Difficulty) : Filter()
    data class Tag(val tag: RecipeTag.ExistingTag) : Filter()
    data class Ingredient(val ingredient: RecipeIngredient.ExistingIngredient) : Filter()
}
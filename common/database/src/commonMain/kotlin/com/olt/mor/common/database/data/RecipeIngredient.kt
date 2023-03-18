package com.olt.mor.common.database.data

sealed interface RecipeIngredient {

    data class NewIngredient(val amount: Double, val unit: IngredientUnit, val name: String) : RecipeIngredient

    data class ExistingIngredient(val id: Long, val name: String, val amount: Double, val unit: IngredientUnit) : RecipeIngredient
}

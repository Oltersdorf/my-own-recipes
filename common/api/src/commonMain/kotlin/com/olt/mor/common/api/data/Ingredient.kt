package com.olt.mor.common.api.data

sealed interface Ingredient {
    data class New(val amount: Double, val unit: IngredientUnit, val name: String) : Ingredient

    data class Existing(val id: Long, val name: String, val amount: Double, val unit: IngredientUnit) : Ingredient
}
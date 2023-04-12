package com.olt.mor.common.api.data

sealed interface IngredientUnit {
    object None : IngredientUnit

    object Volume : IngredientUnit

    object Weight : IngredientUnit

    object Length : IngredientUnit

    object Piece : IngredientUnit

    object TeaSpoon : IngredientUnit

    object TableSpoon : IngredientUnit

    object Pinch : IngredientUnit
}
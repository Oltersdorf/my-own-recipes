package com.olt.mor.common.database.data

import com.squareup.sqldelight.ColumnAdapter

sealed class IngredientUnit {

    object None : IngredientUnit()

    object Volume : IngredientUnit()

    object Weight : IngredientUnit()

    object Length : IngredientUnit()

    object Piece : IngredientUnit()

    object TeaSpoon : IngredientUnit()

    object TableSpoon : IngredientUnit()

    object Pinch : IngredientUnit()

    internal class Adapter : ColumnAdapter<IngredientUnit, String> {
        override fun decode(databaseValue: String): IngredientUnit =
            when (databaseValue) {
                "v" -> Volume
                "w" -> Weight
                "l" -> Length
                "pc" -> Piece
                "teas" -> TeaSpoon
                "tabs" -> TableSpoon
                "pn" -> Pinch
                else -> None
            }

        override fun encode(value: IngredientUnit): String =
            when (value) {
                Length -> "l"
                None -> ""
                Piece -> "pc"
                Pinch -> "pn"
                TableSpoon -> "tabs"
                TeaSpoon -> "teas"
                Volume -> "v"
                Weight -> "w"
            }
    }
}

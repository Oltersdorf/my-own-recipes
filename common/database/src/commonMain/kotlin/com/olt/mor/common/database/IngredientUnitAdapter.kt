package com.olt.mor.common.database

import com.olt.mor.common.api.data.IngredientUnit
import com.squareup.sqldelight.ColumnAdapter

internal class IngredientUnitAdapter : ColumnAdapter<IngredientUnit, String> {
    override fun decode(databaseValue: String): IngredientUnit =
        when (databaseValue) {
            "v" -> IngredientUnit.Volume
            "w" -> IngredientUnit.Weight
            "l" -> IngredientUnit.Length
            "pc" -> IngredientUnit.Piece
            "teas" -> IngredientUnit.TeaSpoon
            "tabs" -> IngredientUnit.TableSpoon
            "pn" -> IngredientUnit.Pinch
            else -> IngredientUnit.None
        }

    override fun encode(value: IngredientUnit): String =
        when (value) {
            IngredientUnit.Length -> "l"
            IngredientUnit.None -> ""
            IngredientUnit.Piece -> "pc"
            IngredientUnit.Pinch -> "pn"
            IngredientUnit.TableSpoon -> "tabs"
            IngredientUnit.TeaSpoon -> "teas"
            IngredientUnit.Volume -> "v"
            IngredientUnit.Weight -> "w"
        }
}
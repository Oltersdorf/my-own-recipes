package com.olt.mor.common.database

import com.olt.mor.common.api.data.Difficulty
import com.squareup.sqldelight.ColumnAdapter

internal class DifficultyAdapter : ColumnAdapter<Difficulty, Long> {
    override fun decode(databaseValue: Long): Difficulty =
        when (databaseValue) {
            1L -> Difficulty.Easy
            2L -> Difficulty.Medium
            3L -> Difficulty.Hard
            else -> Difficulty.NotDefined
        }

    override fun encode(value: Difficulty): Long =
        when (value) {
            Difficulty.NotDefined -> 0L
            Difficulty.Easy -> 1L
            Difficulty.Medium -> 2L
            Difficulty.Hard -> 3L
        }
}
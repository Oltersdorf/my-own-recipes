package com.olt.mor.common.database.data

import com.squareup.sqldelight.ColumnAdapter

sealed class Difficulty {

    object NotDefined : Difficulty()

    object Easy : Difficulty()

    object Medium : Difficulty()

    object Hard : Difficulty()

    internal class Adapter : ColumnAdapter<Difficulty, Long> {
        override fun decode(databaseValue: Long): Difficulty =
            when (databaseValue) {
                1L -> Easy
                2L -> Medium
                3L -> Hard
                else -> NotDefined
            }

        override fun encode(value: Difficulty): Long =
            when (value) {
                NotDefined -> 0L
                Easy -> 1L
                Medium -> 2L
                Hard -> 3L
            }
    }
}

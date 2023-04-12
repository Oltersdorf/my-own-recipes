package com.olt.mor.common.api.data

sealed interface Difficulty {
    object NotDefined : Difficulty

    object Easy : Difficulty

    object Medium : Difficulty

    object Hard : Difficulty
}
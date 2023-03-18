package com.olt.mor.common.database.data

sealed interface RecipeTag {

    data class NewTag(val name: String) : RecipeTag

    data class ExistingTag(val id: Long, val name: String) : RecipeTag
}

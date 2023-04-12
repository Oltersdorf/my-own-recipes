package com.olt.mor.common.api.database

import com.olt.mor.common.api.data.*
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface MORDatabase {
    suspend fun addRecipe(recipe: Recipe)

    suspend fun deleteRecipe(id: Long)

    suspend fun updateRecipe(recipe: Recipe)

    suspend fun selectRecipe(id: Long): Recipe?

    suspend fun searchRecipe(
        name: String = "",
        author: String = "",
        rating: Int = 0,
        maxTime: Int = Int.MAX_VALUE,
        difficulty: Difficulty = Difficulty.NotDefined,
        tags: List<Tag.Existing> = emptyList(),
        ingredients: List<Ingredient.Existing> = emptyList()
    ): List<RecipePreview>

    fun tags(context: CoroutineContext): Flow<List<Tag.Existing>>

    fun ingredients(context: CoroutineContext): Flow<List<Ingredient.Existing>>
}
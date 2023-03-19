package com.olt.mor.common.database

import com.olt.mor.common.database.data.*
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
        tags: List<RecipeTag.ExistingTag> = emptyList(),
        ingredients: List<RecipeIngredient.ExistingIngredient> = emptyList()
    ): List<PreviewRecipe>

    fun tags(context: CoroutineContext): Flow<List<RawTag>>

    fun ingredients(context: CoroutineContext): Flow<List<RawIngredient>>
}
package com.olt.mor.common.search.integration

import com.olt.mor.common.database.MORDatabase
import com.olt.mor.common.database.RawIngredient
import com.olt.mor.common.database.RawTag
import com.olt.mor.common.database.data.Difficulty
import com.olt.mor.common.database.data.PreviewRecipe
import com.olt.mor.common.database.data.RecipeIngredient
import com.olt.mor.common.database.data.RecipeTag
import com.olt.mor.common.search.store.MORSearchStoreProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MORSearchStoreDatabase(
    private val database: MORDatabase
) : MORSearchStoreProvider.Database {

    private val recipesFlow = MutableStateFlow<List<PreviewRecipe>>(emptyList())

    override val recipes: Flow<List<PreviewRecipe>> = recipesFlow.asStateFlow()

    override val tags: Flow<List<RawTag>> = database.tags(Dispatchers.IO)

    override val ingredients: Flow<List<RawIngredient>> = database.ingredients(Dispatchers.IO)

    override suspend fun searchRecipes(
        name: String,
        author: String,
        rating: Int,
        maxTime: Int,
        difficulty: Difficulty,
        tags: List<RecipeTag.ExistingTag>,
        ingredients: List<RecipeIngredient.ExistingIngredient>
    ) {
        val previews = database.searchRecipe(
            name = name,
            author = author,
            rating = rating,
            maxTime = maxTime,
            difficulty = difficulty,
            tags = tags,
            ingredients = ingredients
        )

        recipesFlow.emit(previews)
    }
}
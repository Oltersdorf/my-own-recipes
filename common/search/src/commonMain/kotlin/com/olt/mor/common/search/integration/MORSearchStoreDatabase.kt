package com.olt.mor.common.search.integration

import com.olt.mor.common.api.data.Difficulty
import com.olt.mor.common.api.data.Ingredient
import com.olt.mor.common.api.data.RecipePreview
import com.olt.mor.common.api.data.Tag
import com.olt.mor.common.api.database.MORDatabase
import com.olt.mor.common.search.store.MORSearchStoreProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MORSearchStoreDatabase(
    private val database: MORDatabase
) : MORSearchStoreProvider.Database {

    private val recipesFlow = MutableStateFlow<List<RecipePreview>>(emptyList())

    override val recipes: Flow<List<RecipePreview>> = recipesFlow.asStateFlow()

    override val tags: Flow<List<Tag.Existing>> = database.tags(Dispatchers.IO)

    override val ingredients: Flow<List<Ingredient.Existing>> = database.ingredients(Dispatchers.IO)

    override suspend fun searchRecipes(
        name: String,
        author: String,
        rating: Int,
        maxTime: Int,
        difficulty: Difficulty,
        tags: List<Tag.Existing>,
        ingredients: List<Ingredient.Existing>
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
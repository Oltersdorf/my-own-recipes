package com.olt.mor.common.database

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.observable.Observable
import com.olt.mor.common.database.data.*

interface MyOwnRecipesDatabase {

    fun addRecipe(recipe: Recipe): Completable

    fun deleteRecipe(id: Long): Completable

    fun updateRecipe(recipe: Recipe): Completable

    fun selectRecipe(id: Long): Maybe<Recipe>

    fun searchRecipe(
        name: String = "",
        author: String = "",
        rating: Int = 0,
        maxTime: Int = Int.MAX_VALUE,
        difficulty: Difficulty = Difficulty.NotDefined,
        tags: List<RecipeTag.ExistingTag> = emptyList(),
        ingredients: List<RecipeIngredient.ExistingIngredient> = emptyList()
    ): Maybe<List<PreviewRecipe>>

    fun observeTags(): Observable<List<RawTag>>

    fun observeIngredients(): Observable<List<RawIngredient>>
}
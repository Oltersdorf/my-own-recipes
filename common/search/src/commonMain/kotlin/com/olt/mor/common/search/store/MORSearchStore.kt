package com.olt.mor.common.search.store

import com.arkivanov.mvikotlin.core.store.Store
import com.olt.mor.common.api.data.Ingredient
import com.olt.mor.common.api.data.RecipePreview
import com.olt.mor.common.api.data.Tag
import com.olt.mor.common.search.Filter
import com.olt.mor.common.search.store.MORSearchStore.Intent
import com.olt.mor.common.search.store.MORSearchStore.State

internal interface MORSearchStore : Store<Intent, State, Nothing> {

    sealed class Intent {
        data class ChangeSearchTerm(val searchTerm: String) : Intent()
        data class AddFilter(val filter: Filter) : Intent()
        data class RemoveFilter(val filter: Filter) : Intent()
    }

    data class State(
        val tags: List<Tag.Existing> = emptyList(),
        val ingredients: List<Ingredient.Existing> = emptyList(),
        val searchTerm: String = "",
        val filterRecommendations: List<Filter> = emptyList(),
        val filters: List<Filter> = emptyList(),
        val recipes: List<RecipePreview> = emptyList()
    )
}
package com.olt.mor.common.search.store

import com.arkivanov.mvikotlin.core.store.Store
import com.olt.mor.common.database.RawIngredient
import com.olt.mor.common.database.RawTag
import com.olt.mor.common.database.data.PreviewRecipe
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
        val tags: List<RawTag> = emptyList(),
        val ingredients: List<RawIngredient> = emptyList(),
        val searchTerm: String = "",
        val filterRecommendations: List<Filter> = emptyList(),
        val filters: List<Filter> = emptyList(),
        val recipes: List<PreviewRecipe> = emptyList()
    )
}
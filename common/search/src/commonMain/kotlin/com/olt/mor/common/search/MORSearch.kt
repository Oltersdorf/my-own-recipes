package com.olt.mor.common.search

import com.arkivanov.decompose.value.Value
import com.olt.mor.common.api.data.RecipePreview

interface MORSearch {

    val models: Value<Model>

    fun onSearchTermChanged(searchTerm: String)

    fun onFilterAdd(filter: Filter)

    fun onFilterRemove(filter: Filter)

    data class Model(
        val searchTerm: String,
        val filterRecommendations: List<Filter>,
        val filters: List<Filter>,
        val recipes: List<RecipePreview>
    )
}
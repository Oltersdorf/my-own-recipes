package com.olt.mor.common.search.integration

import com.olt.mor.common.search.MORSearch.Model
import com.olt.mor.common.search.store.MORSearchStore.State

internal val stateToModel: (State) -> Model =
    {
        Model(
            searchTerm = it.searchTerm,
            filterRecommendations = it.filterRecommendations,
            filters = it.filters,
            recipes = it.recipes
        )
    }

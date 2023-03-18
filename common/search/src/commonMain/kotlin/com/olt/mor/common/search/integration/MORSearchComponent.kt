package com.olt.mor.common.search.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.olt.mor.common.search.MORSearch
import com.olt.mor.common.search.MORSearch.Model
import com.olt.mor.common.search.store.MORSearchStore.Intent
import com.olt.mor.common.search.store.MORSearchStoreProvider

class MORSearchComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
) : MORSearch, ComponentContext by componentContext {

    private val store =
        instanceKeeper.getStore {
            MORSearchStoreProvider(
                storeFactory = storeFactory
            ).provide()
        }

    override val models: Value<Model> = store.asValue().map(stateToModel)

    override fun onSearchTermChanged(newText: String) {
        store.accept(Intent.ChangeSearchTerm(newSearchTerm = newText))
    }
}
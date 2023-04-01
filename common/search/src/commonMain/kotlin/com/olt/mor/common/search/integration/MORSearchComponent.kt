package com.olt.mor.common.search.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.olt.mor.common.database.MORDatabase
import com.olt.mor.common.search.Filter
import com.olt.mor.common.search.MORSearch
import com.olt.mor.common.search.MORSearch.Model
import com.olt.mor.common.search.store.MORSearchStore.Intent
import com.olt.mor.common.search.store.MORSearchStoreProvider
import com.olt.mor.common.utils.store.asValue

class MORSearchComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    database: MORDatabase
) : MORSearch, ComponentContext by componentContext {

    private val store =
        instanceKeeper.getStore {
            MORSearchStoreProvider(
                storeFactory = storeFactory,
                database = MORSearchStoreDatabase(database = database)
            ).provide()
        }

    override val models: Value<Model> = store.asValue().map(stateToModel)

    override fun onSearchTermChanged(searchTerm: String) {
        store.accept(Intent.ChangeSearchTerm(searchTerm = searchTerm))
    }

    override fun onFilterAdd(filter: Filter) {
        store.accept(Intent.AddFilter(filter = filter))
    }

    override fun onFilterRemove(filter: Filter) {
        store.accept(Intent.RemoveFilter(filter = filter))
    }
}
package com.olt.mor.common.search.store

import com.arkivanov.mvikotlin.core.store.Store
import com.olt.mor.common.search.store.MORSearchStore.Intent
import com.olt.mor.common.search.store.MORSearchStore.State

internal interface MORSearchStore : Store<Intent, State, Nothing> {

    sealed class Intent {
        data class ChangeSearchTerm(val newSearchTerm: String) : Intent()
    }

    data class State(
        val searchTerm: String = ""
    )
}
package com.olt.mor.common.search.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.ReaktiveExecutor
import com.olt.mor.common.search.store.MORSearchStore.Intent
import com.olt.mor.common.search.store.MORSearchStore.State

internal class MORSearchStoreProvider(
    private val storeFactory: StoreFactory
) {

    fun provide(): MORSearchStore =
        object : MORSearchStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "MORSearchStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed class Message {
        data class SearchTermChanged(val newSearchTerm: String) : Message()
    }

    private inner class ExecutorImpl : ReaktiveExecutor<Intent, Unit, State, Message, Nothing>() {
        override fun executeIntent(intent: Intent, getState: () -> State): Unit =
            when (intent) {
                is Intent.ChangeSearchTerm -> dispatch(Message.SearchTermChanged(newSearchTerm = intent.newSearchTerm))
            }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State =
            when (msg) {
                is Message.SearchTermChanged -> copy(searchTerm = msg.newSearchTerm)
            }
    }
}
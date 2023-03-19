package com.olt.mor.common.search.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.olt.mor.common.search.store.MORSearchStore.Intent
import kotlin.test.Test
import kotlin.test.assertEquals

class MORSearchStoreTest {
    private val provider = MORSearchStoreProvider(storeFactory = DefaultStoreFactory())

    @Test
    fun `WHEN Intent ChangeSearchTerm THEN searchTerm changed in state`() {
        val store = provider.provide()

        store.accept(Intent.ChangeSearchTerm("new text"))

        assertEquals("new text", store.state.searchTerm)
    }
}
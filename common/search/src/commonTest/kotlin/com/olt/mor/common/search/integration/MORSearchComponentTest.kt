package com.olt.mor.common.search.integration

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.badoo.reaktive.scheduler.overrideSchedulers
import com.badoo.reaktive.test.scheduler.TestScheduler
import com.olt.mor.common.search.MORSearch
import com.olt.mor.common.search.MORSearch.Model
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MORSearchComponentTest {

    private val lifecycle = LifecycleRegistry()
    private lateinit var morSearch: MORSearch
    private val model: Model get() = morSearch.models.value

    @BeforeTest
    fun before() {
        overrideSchedulers(
            main = { TestScheduler() },
            io = { TestScheduler() }
        )

        morSearch = MORSearchComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            storeFactory = DefaultStoreFactory()
        )
    }

    @Test
    fun `WHEN search term changed THEN model is updated`() {
         morSearch.onSearchTermChanged("new text")

        assertEquals("new text", model.searchTerm)
    }
}
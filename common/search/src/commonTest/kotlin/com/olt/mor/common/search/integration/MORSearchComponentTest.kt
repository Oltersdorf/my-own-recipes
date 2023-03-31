package com.olt.mor.common.search.integration

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.olt.mor.common.database.DefaultMORDatabase
import com.olt.mor.common.search.Filter
import com.olt.mor.common.search.MORSearch
import com.olt.mor.common.search.MORSearch.Model
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MORSearchComponentTest {

    private val lifecycle = LifecycleRegistry()
    private lateinit var morSearch: MORSearch
    private val model: Model get() = morSearch.models.value
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun before() {
        Dispatchers.setMain(testDispatcher)
        morSearch = MORSearchComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            storeFactory = DefaultStoreFactory(),
            database = DefaultMORDatabase(driver = testDriver())
        )
    }

    @AfterTest
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN search term changed THEN model is updated`() {
         morSearch.onSearchTermChanged("new text")

        assertEquals("new text", model.searchTerm)
    }

    @Test
    fun `WHEN filter add THEN model is updated`() {
        morSearch.onFilterAdd(Filter.Name("test"))

        assertEquals(listOf(Filter.Name("test")), model.filters)
    }

    @Test
    fun `WHEN filter removed THEN model is updated`() {
        morSearch.onFilterAdd(Filter.Name("test"))
        morSearch.onFilterRemove(Filter.Name("test"))

        assertEquals(emptyList(), model.filters)
    }
}

expect fun testDriver(): SqlDriver
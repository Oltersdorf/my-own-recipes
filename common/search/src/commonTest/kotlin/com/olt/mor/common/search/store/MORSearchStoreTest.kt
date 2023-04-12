package com.olt.mor.common.search.store

import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.olt.mor.common.api.data.*
import com.olt.mor.common.search.Filter
import com.olt.mor.common.search.store.MORSearchStore.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MORSearchStoreTest {
    private class TestDatabase : MORSearchStoreProvider.Database {

        val recipesEmitter = MutableStateFlow<List<RecipePreview>>(emptyList())

        override val recipes: Flow<List<RecipePreview>>
            get() = recipesEmitter.asStateFlow()

        val tagsEmitter = MutableStateFlow<List<Tag.Existing>>(emptyList())

        override val tags: Flow<List<Tag.Existing>>
            get() = tagsEmitter.asStateFlow()

        val ingredientsEmitter = MutableStateFlow<List<Ingredient.Existing>>(emptyList())

        override val ingredients: Flow<List<Ingredient.Existing>>
            get() = ingredientsEmitter.asStateFlow()

        override suspend fun searchRecipes(
            name: String,
            author: String,
            rating: Int,
            maxTime: Int,
            difficulty: Difficulty,
            tags: List<Tag.Existing>,
            ingredients: List<Ingredient.Existing>
        ) {

        }
    }

    private lateinit var testDatabase: TestDatabase
    private lateinit var storeProvider: MORSearchStoreProvider
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun before() {
        Dispatchers.setMain(testDispatcher)
        testDatabase = TestDatabase()
        storeProvider = MORSearchStoreProvider(storeFactory = DefaultStoreFactory(), database = testDatabase)
    }

    @AfterTest
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN tags in database change THEN state is changed`() = runTest {
        val expected = listOf(Tag.Existing(id = 1L, name = "tag1"))
        testDatabase.tagsEmitter.emit(expected)

        val store = storeProvider.provide()
        val actual = mutableListOf<MORSearchStore.State>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            store.states.toList(actual)
        }

        assertEquals(listOf(expected), actual.map { it.tags })
    }

    @Test
    fun `WHEN ingredients in database change THEN state is changed`() = runTest {
        val expected = listOf(Ingredient.Existing(id = 1L, name = "ingredient1", amount = 0.0, unit = IngredientUnit.None))
        testDatabase.ingredientsEmitter.emit(expected)

        val store = storeProvider.provide()
        val actual = mutableListOf<MORSearchStore.State>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            store.states.toList(actual)
        }

        assertEquals(listOf(expected), actual.map { it.ingredients })
    }

    @Test
    fun `WHEN recipes in database change THEN state is changed`() = runTest {
        val expected = listOf(RecipePreview(id = 1L, name = "recipe1", author = "a", rating = 1, difficulty = Difficulty.NotDefined, time = 0, tags = emptyList()))
        testDatabase.recipesEmitter.emit(expected)

        val store = storeProvider.provide()
        val actual = mutableListOf<MORSearchStore.State>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            store.states.toList(actual)
        }

        assertEquals(listOf(expected), actual.map { it.recipes })
    }

    @Test
    fun `WHEN Intent ChangeSearchTerm THEN searchTerm changed in state`() = runTest {
        val store = storeProvider.provide()
        store.accept(Intent.ChangeSearchTerm("new text"))

        assertEquals("new text", store.state.searchTerm)
    }

    @Test
    fun `WHEN Intent ChangeSearchTerm THEN correct text recommendations are shown`() = runTest {
        val searchTerm = "search"

        testDatabase.tagsEmitter.emit(listOf(Tag.Existing(id = 1L, name = "$searchTerm tag"), Tag.Existing(id = 2L, "different")))
        testDatabase.ingredientsEmitter.emit(listOf(Ingredient.Existing(id = 2L, name = "ingredient $searchTerm", amount = 0.0, unit = IngredientUnit.None), Ingredient.Existing(id = 1L, name = "diff", amount = 0.0, unit = IngredientUnit.None)))

        val store = storeProvider.provide()
        store.accept(Intent.ChangeSearchTerm(searchTerm))

        val expected = listOf(
            Filter.Name(searchTerm),
            Filter.Author(searchTerm),
            Filter.Tag(Tag.Existing(id = 1L, name = "$searchTerm tag")),
            Filter.Ingredient(Ingredient.Existing(id = 2L, name = "ingredient $searchTerm", amount = 0.0, unit = IngredientUnit.None))
        )
        assertContentEquals(expected, store.state.filterRecommendations)
    }

    @Test
    fun `WHEN Intent ChangeSearchTerm THEN correct number recommendations are shown`() = runTest {
        val searchTerm = 1

        testDatabase.tagsEmitter.emit(listOf(Tag.Existing(id = 1L, name = "$searchTerm tag"), Tag.Existing(id = 2L, "different")))
        testDatabase.ingredientsEmitter.emit(listOf(Ingredient.Existing(id = 2L, name = "ingredient $searchTerm", amount = 0.0, unit = IngredientUnit.None), Ingredient.Existing(id = 1L, name = "diff", amount = 0.0, unit = IngredientUnit.None)))

        val store = storeProvider.provide()
        store.accept(Intent.ChangeSearchTerm(searchTerm.toString()))

        val expected = listOf(
            Filter.Rating(searchTerm),
            Filter.MaxTime(searchTerm),
            Filter.Difficulty(Difficulty.Easy),
            Filter.Name(searchTerm.toString()),
            Filter.Author(searchTerm.toString()),
            Filter.Tag(Tag.Existing(id = 1L, name = "$searchTerm tag")),
            Filter.Ingredient(Ingredient.Existing(id = 2L, name = "ingredient $searchTerm", amount = 0.0, unit = IngredientUnit.None)),
        )
        assertContentEquals(expected, store.state.filterRecommendations)
    }

    @Test
    fun `WHEN Intent AddFilter THEN filter is added in state`() {
        val store = storeProvider.provide()
        store.accept(Intent.ChangeSearchTerm("test"))
        store.accept(Intent.AddFilter(Filter.Name("test")))

        assertEquals(MORSearchStore.State(filters = listOf(Filter.Name("test"))), store.state)
    }

    @Test
    fun `WHEN Intent RemoveFilter THEN filter is removed in state`() {
        val store = storeProvider.provide()
        store.accept(Intent.AddFilter(Filter.Name("test")))
        store.accept(Intent.RemoveFilter(Filter.Name("test")))

        assertEquals(emptyList(), store.state.filters)
    }
}
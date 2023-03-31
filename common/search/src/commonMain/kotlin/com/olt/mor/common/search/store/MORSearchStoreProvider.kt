package com.olt.mor.common.search.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.olt.mor.common.database.RawIngredient
import com.olt.mor.common.database.RawTag
import com.olt.mor.common.database.data.*
import com.olt.mor.common.search.Filter
import com.olt.mor.common.search.store.MORSearchStore.Intent
import com.olt.mor.common.search.store.MORSearchStore.State
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

internal class MORSearchStoreProvider(
    private val storeFactory: StoreFactory,
    private val database: Database
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
        data class TagsLoaded(val tags: List<RawTag>) : Message()
        data class IngredientsLoaded(val ingredients: List<RawIngredient>) : Message()
        data class SearchTermChanged(val newSearchTerm: String) : Message()
        data class RecipesLoaded(val recipes: List<PreviewRecipe>) : Message()
        data class FilterAdded(val filter: Filter) : Message()
        data class FilterRemoved(val filter: Filter) : Message()
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Unit, State, Message, Nothing>() {
        override fun executeAction(action: Unit, getState: () -> State) {
            scope.launch {
                database.recipes.collectLatest { dispatch(Message.RecipesLoaded(it)) }
            }
            scope.launch {
                database.tags.collectLatest { dispatch(Message.TagsLoaded(it)) }
            }
            scope.launch {
                database.ingredients.collectLatest { dispatch(Message.IngredientsLoaded(it)) }
            }
        }

        override fun executeIntent(intent: Intent, getState: () -> State): Unit =
            when (intent) {
                is Intent.ChangeSearchTerm -> dispatch(Message.SearchTermChanged(newSearchTerm = intent.searchTerm))
                is Intent.AddFilter -> addFilter(newFilter = intent.filter, existingFilters = getState().filters)
                is Intent.RemoveFilter -> removeFilter(oldFilter = intent.filter, existingFilters = getState().filters)
            }

        private fun addFilter(newFilter: Filter, existingFilters: List<Filter>) {
            dispatch(Message.FilterAdded(filter = newFilter))
            scope.launch(Dispatchers.IO) {
                val allFilter = existingFilters.toMutableList()
                allFilter.add(newFilter)

                search(allFilter)
            }
        }

        private fun removeFilter(oldFilter: Filter, existingFilters: List<Filter>) {
            dispatch(Message.FilterRemoved(filter = oldFilter))
            scope.launch(Dispatchers.IO) {
                val allFilter = existingFilters.toMutableList()
                allFilter.remove(oldFilter)

                search(allFilter)
            }
        }

        private suspend fun search(filter: List<Filter>) {
            database.searchRecipes(
                name = filter.filterIsInstance<Filter.Name>().firstOrNull()?.name ?: "",
                author = filter.filterIsInstance<Filter.Author>().firstOrNull()?.author ?: "",
                rating = filter.filterIsInstance<Filter.Rating>().firstOrNull()?.rating ?: 0,
                maxTime = filter.filterIsInstance<Filter.MaxTime>().firstOrNull()?.maxTime ?: 0,
                difficulty = filter.filterIsInstance<Filter.Difficulty>().firstOrNull()?.difficulty ?: Difficulty.NotDefined,
                tags = filter.filterIsInstance<Filter.Tag>().map { it.tag },
                ingredients = filter.filterIsInstance<Filter.Ingredient>().map { it.ingredient }
            )
        }
    }

    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(msg: Message): State =
            when (msg) {
                is Message.TagsLoaded -> copy(tags = msg.tags)
                is Message.IngredientsLoaded -> copy(ingredients = msg.ingredients)
                is Message.SearchTermChanged -> copy(searchTerm = msg.newSearchTerm, filterRecommendations = getRecommendedFilters(searchTerm = msg.newSearchTerm, availableIngredients = ingredients, availableTags = tags))
                is Message.FilterAdded -> copy(searchTerm = "", filterRecommendations = emptyList(), filters = filters.toMutableList().apply { add(msg.filter) }.toList())
                is Message.FilterRemoved -> copy(filters = filters.toMutableList().apply { remove(msg.filter) }.toList())
                is Message.RecipesLoaded -> copy(recipes = msg.recipes)
            }

        private fun getRecommendedFilters(searchTerm: String, availableTags: List<RawTag>, availableIngredients: List<RawIngredient>): List<Filter> {
            val number = searchTerm.toIntOrNull()
            val filter = mutableListOf<Filter>()
            if (number != null && number >= 0) filter.addAll(getRecommendedNumberFilter(number))
            filter.addAll(getRecommendedTextFilter(text = searchTerm, availableTags = availableTags, availableIngredients = availableIngredients))

            return filter.toList()
        }

        private fun getRecommendedNumberFilter(number: Int): List<Filter> {
            val filter = mutableListOf(Filter.Rating(number), Filter.MaxTime(number))

            when (number) {
                1 -> filter.add(Filter.Difficulty(Difficulty.Easy))
                2 -> filter.add(Filter.Difficulty(Difficulty.Medium))
                3 -> filter.add(Filter.Difficulty(Difficulty.Hard))
                else -> {}
            }

            return filter.toList()
        }

        private fun getRecommendedTextFilter(text: String, availableTags: List<RawTag>, availableIngredients: List<RawIngredient>): List<Filter> {
            val filter = mutableListOf(Filter.Name(text), Filter.Author(text))

            filter.addAll(
                availableTags
                    .filter { it.name.contains(other = text, ignoreCase = true) }
                    .map { Filter.Tag(RecipeTag.ExistingTag(id = it.id, name = it.name)) }
            )

            filter.addAll(
                availableIngredients
                    .filter { it.name.contains(other = text, ignoreCase = true) }
                    .map { Filter.Ingredient(RecipeIngredient.ExistingIngredient(id = it.id, name = it.name, amount = 0.0, unit = IngredientUnit.None)) }
            )

            return filter.toList()
        }
    }

    interface Database {
        val recipes: Flow<List<PreviewRecipe>>

        val tags: Flow<List<RawTag>>

        val ingredients: Flow<List<RawIngredient>>

        suspend fun searchRecipes(
            name: String,
            author: String,
            rating: Int,
            maxTime: Int,
            difficulty: Difficulty,
            tags: List<RecipeTag.ExistingTag>,
            ingredients: List<RecipeIngredient.ExistingIngredient>
        )
    }
}
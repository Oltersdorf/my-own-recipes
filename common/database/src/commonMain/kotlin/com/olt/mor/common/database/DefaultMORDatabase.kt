package com.olt.mor.common.database

import com.olt.mor.common.api.data.*
import com.olt.mor.common.api.database.MORDatabase
import com.olt.mor.database.MyOwnRecipes
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

class DefaultMORDatabase(
    private val database: MyOwnRecipes
) : MORDatabase {

    constructor(driver: SqlDriver) : this(
        database = MyOwnRecipes(
            driver = driver,
            RawRecipeAdapter = RawRecipe.Adapter(difficultyAdapter = DifficultyAdapter()),
            RecipeToIngredientAdapter = RecipeToIngredient.Adapter(unitAdapter = IngredientUnitAdapter())
        )
    )

    private val recipeQueries: RecipeQueries
        get() = database.recipeQueries

    override suspend fun addRecipe(recipe: Recipe): Unit =
        recipeQueries.transaction {
            recipeQueries.addRecipe(
                name = recipe.name,
                author = recipe.author,
                rating = recipe.rating,
                workTimeInMinutes = recipe.workTimeInMinutes,
                cookTimeInMinutes = recipe.cookTimeInMinutes,
                difficulty = recipe.difficulty,
                portions = recipe.portions,
                text = recipe.text
            )

            val recipeId = recipeQueries.getLastInsertId().executeAsOne()

            recipeQueries.addTagsAndIngredients(recipeId = recipeId, tags = recipe.tags, ingredients = recipe.ingredients)
        }

    override suspend fun deleteRecipe(id: Long): Unit =
        recipeQueries.transaction {
            recipeQueries.deleteRecipe(id = id)
            recipeQueries.removeUnusedTags()
            recipeQueries.removeUnusedIngredients()
        }

    override suspend fun selectRecipe(id: Long): Recipe? =
        recipeQueries.transactionWithResult {
            val tags = recipeQueries.selectTags(id = id)
                .executeAsList()
                .map { tag -> Tag.Existing(id = tag.id, name = tag.name) }
            val ingredients = recipeQueries.selectIngredients(id = id)
                .executeAsList()
                .map { ingredient ->
                    Ingredient.Existing(
                        name = ingredient.name,
                        amount = ingredient.amount,
                        unit = ingredient.unit,
                        id = ingredient.id
                    )
                }
            val recipe = recipeQueries.selectRecipe(id = id).executeAsOneOrNull()

            if (recipe == null) null
            else Recipe(
                id = recipe.id,
                name = recipe.name,
                author = recipe.author,
                rating = recipe.rating,
                workTimeInMinutes = recipe.workTimeInMinutes,
                cookTimeInMinutes = recipe.cookTimeInMinutes,
                difficulty = recipe.difficulty,
                portions = recipe.portions,
                text = recipe.text,
                tags = tags,
                ingredients = ingredients
            )
        }

    override suspend fun updateRecipe(recipe: Recipe): Unit =
        recipeQueries.transaction {
            recipeQueries.updateRecipe(
                id = recipe.id,
                name = recipe.name,
                author = recipe.author,
                rating = recipe.rating,
                workTimeInMinutes = recipe.workTimeInMinutes,
                cookTimeInMinutes = recipe.cookTimeInMinutes,
                difficulty = recipe.difficulty,
                portions = recipe.portions,
                text = recipe.text
            )

            recipeQueries.deleteTagLink(id = recipe.id)
            recipeQueries.deleteIngredientLink(id = recipe.id)

            recipeQueries.addTagsAndIngredients(recipeId = recipe.id, tags = recipe.tags, ingredients = recipe.ingredients)

            recipeQueries.removeUnusedTags()
            recipeQueries.removeUnusedIngredients()
        }

    override suspend fun searchRecipe(
        name: String,
        author: String,
        rating: Int,
        maxTime: Int,
        difficulty: Difficulty,
        tags: List<Tag.Existing>,
        ingredients: List<Ingredient.Existing>
    ): List<RecipePreview> =
        recipeQueries.transactionWithResult {
            val tagsMap = recipeQueries.selectAllTags().executeAsList().associate { tag -> tag.id to tag.name }

            recipeQueries.searchRecipe(
                    tags = tags.map { tag -> tag.id },
                    ingredients = ingredients.map { ingredient -> ingredient.id },
                    name = name,
                    author = author,
                    rating = rating,
                    time = maxTime.toLong(),
                    difficulty = difficulty,
                    filterTags = if (tags.isEmpty()) 0L else 1L,
                    filterIngredients = if (ingredients.isEmpty()) 0L else 1L
                )
                .executeAsList()
                .map { recipe ->
                    RecipePreview(
                        id = recipe.id,
                        name = recipe.name,
                        author = recipe.author,
                        rating = recipe.rating,
                        difficulty = recipe.difficulty,
                        time = recipe.time.toInt(),
                        tags = recipe.tags?.split(",")?.map { id -> Tag.Existing(id = id.toLong(), name = tagsMap[id.toLong()] ?: "") } ?: emptyList(),
                    )
                }
        }

    override fun ingredients(context: CoroutineContext): Flow<List<Ingredient.Existing>> =
        database.ingredientQueries
            .selectAll()
            .asFlow()
            .mapToList(context = context)
            .map { ingredientsList ->
                ingredientsList.map {
                    Ingredient.Existing(
                        id = it.id,
                        name = it.name,
                        amount = 0.0,
                        unit = IngredientUnit.None
                    )
                }
            }

    override fun tags(context: CoroutineContext): Flow<List<Tag.Existing>> =
        database.tagQueries
            .selectAll()
            .asFlow()
            .mapToList(context = context)
            .map { tagsList ->
                tagsList.map {
                    Tag.Existing(
                        id = it.id,
                        name = it.name
                    )
                }
            }

    private fun RecipeQueries.addTagsAndIngredients(recipeId: Long, tags: List<Tag>, ingredients: List<Ingredient>) {
        tags.forEach { tag ->
            when (tag) {
                is Tag.Existing -> addTagLink(recipeId = recipeId, tagId = tag.id)
                is Tag.New -> {
                    addTag(name = tag.name)
                    addTagLink(recipeId = recipeId, tagId = getLastInsertId().executeAsOne())
                }
            }
        }

        ingredients.forEach { ingredient ->
            when (ingredient) {
                is Ingredient.Existing -> addIngredientLink(
                    recipeId = recipeId,
                    ingredientId = ingredient.id,
                    amount = ingredient.amount,
                    unit = ingredient.unit
                )
                is Ingredient.New -> {
                    addIngredient(name = ingredient.name)
                    addIngredientLink(
                        recipeId = recipeId,
                        ingredientId = getLastInsertId().executeAsOne(),
                        amount = ingredient.amount,
                        unit = ingredient.unit
                    )
                }
            }
        }
    }
}
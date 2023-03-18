package com.olt.mor.common.database

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.single.*
import com.olt.mor.common.database.data.*
import com.olt.mor.database.MyOwnRecipes
import com.squareup.sqldelight.db.SqlDriver

class DefaultMyOwnRecipesDatabase(
    database: MyOwnRecipes
) : SqlDatabase<MyOwnRecipes>(
    database = database
), MyOwnRecipesDatabase {

    constructor(driver: SqlDriver) : this(
        database = MyOwnRecipes(
            driver = driver,
            RawRecipeAdapter = RawRecipe.Adapter(difficultyAdapter = Difficulty.Adapter()),
            RecipeToIngredientAdapter = RecipeToIngredient.Adapter(unitAdapter = IngredientUnit.Adapter())
        )
    )

    private val recipeQueries: Single<RecipeQueries> =
        databaseQuery { it.recipeQueries }

    override fun addRecipe(recipe: Recipe): Completable =
        execute(query = recipeQueries) {
            it.transaction {
                it.addRecipe(
                    name = recipe.name,
                    author = recipe.author,
                    rating = recipe.rating,
                    workTimeInMinutes = recipe.workTimeInMinutes,
                    cookTimeInMinutes = recipe.cookTimeInMinutes,
                    difficulty = recipe.difficulty,
                    portions = recipe.portions,
                    text = recipe.text
                )

                val recipeId = it.getLastInsertId().executeAsOne()

                it.addTagsAndIngredients(recipeId = recipeId, tags = recipe.tags, ingredients = recipe.ingredients)
            }
        }

    override fun deleteRecipe(id: Long): Completable =
        execute(query = recipeQueries) {
            it.transaction {
                it.deleteRecipe(id = id)
                it.removeUnusedTags()
                it.removeUnusedIngredients()
            }
        }

    override fun selectRecipe(id: Long): Maybe<Recipe> =
        queryMaybe(query = recipeQueries) {
            it.transactionWithResult {
                val tags = it
                    .selectTags(id = id)
                    .executeAsList()
                    .map { tag -> RecipeTag.ExistingTag(id = tag.id, name = tag.name) }
                val ingredients = it
                    .selectIngredients(id = id)
                    .executeAsList()
                    .map { ingredient ->
                        RecipeIngredient.ExistingIngredient(
                            name = ingredient.name,
                            amount = ingredient.amount,
                            unit = ingredient.unit,
                            id = ingredient.id
                        )
                    }
                val recipe = it.selectRecipe(id = id).executeAsOneOrNull()

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
        }

    override fun updateRecipe(recipe: Recipe): Completable =
        execute(query = recipeQueries) {
            it.transaction {
                it.updateRecipe(
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

                it.deleteTagLink(id = recipe.id)
                it.deleteIngredientLink(id = recipe.id)

                it.addTagsAndIngredients(recipeId = recipe.id, tags = recipe.tags, ingredients = recipe.ingredients)

                it.removeUnusedTags()
                it.removeUnusedIngredients()
            }
        }

    override fun searchRecipe(
        name: String,
        author: String,
        rating: Int,
        maxTime: Int,
        difficulty: Difficulty,
        tags: List<RecipeTag.ExistingTag>,
        ingredients: List<RecipeIngredient.ExistingIngredient>
    ): Maybe<List<PreviewRecipe>> =
        queryMaybe(query = recipeQueries) {
            it.transactionWithResult {
                val tagsMap = it.selectAllTags().executeAsList().associate { tag -> tag.id to tag.name }

                it
                    .searchRecipe(
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
                        PreviewRecipe(
                            id = recipe.id,
                            name = recipe.name,
                            author = recipe.author,
                            rating = recipe.rating,
                            difficulty = recipe.difficulty,
                            time = recipe.time.toInt(),
                            tags = recipe.tags?.split(",")?.map { id -> RecipeTag.ExistingTag(id = id.toLong(), name = tagsMap[id.toLong()] ?: "") } ?: emptyList(),
                        )
                    }
            }
        }

    private val ingredientQueries: Single<IngredientQueries> =
        databaseQuery { it.ingredientQueries }

    override fun observeIngredients(): Observable<List<RawIngredient>> =
        queryObservable(query = ingredientQueries) { it.selectAll() }

    private val tagQueries: Single<TagQueries> =
        databaseQuery { it.tagQueries }

    override fun observeTags(): Observable<List<RawTag>> =
        queryObservable(query = tagQueries) { it.selectAll() }

    private fun RecipeQueries.addTagsAndIngredients(recipeId: Long, tags: List<RecipeTag>, ingredients: List<RecipeIngredient>) {
        tags.forEach { tag ->
            when (tag) {
                is RecipeTag.ExistingTag -> addTagLink(recipeId = recipeId, tagId = tag.id)
                is RecipeTag.NewTag -> {
                    addTag(name = tag.name)
                    addTagLink(recipeId = recipeId, tagId = getLastInsertId().executeAsOne())
                }
            }
        }

        ingredients.forEach { ingredient ->
            when (ingredient) {
                is RecipeIngredient.ExistingIngredient -> addIngredientLink(
                    recipeId = recipeId,
                    ingredientId = ingredient.id,
                    amount = ingredient.amount,
                    unit = ingredient.unit
                )
                is RecipeIngredient.NewIngredient -> {
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
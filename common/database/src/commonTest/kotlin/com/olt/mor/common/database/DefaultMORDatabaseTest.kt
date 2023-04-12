package com.olt.mor.common.database

import com.olt.mor.common.api.data.*
import com.olt.mor.database.MyOwnRecipes
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMORDatabaseTest {

    private lateinit var driver: SqlDriver
    private lateinit var testDatabase : DefaultMORDatabase
    private lateinit var actualDatabase : MyOwnRecipes

    @BeforeTest
    fun before() {
        driver = createDriver()
        actualDatabase = MyOwnRecipes(
            driver = driver,
            RawRecipeAdapter = RawRecipe.Adapter(difficultyAdapter = DifficultyAdapter()),
            RecipeToIngredientAdapter = RecipeToIngredient.Adapter(unitAdapter = IngredientUnitAdapter())
        )
        testDatabase = DefaultMORDatabase(database = actualDatabase)
    }

    @AfterTest
    fun after() {
        driver.close()
    }

    @Test
    fun `WHEN recipe is added THEN recipe is written to the database`() = runTest {
        val recipe = buildRecipe()

        testDatabase.addRecipe(recipe = recipe)
        advanceUntilIdle()

        val actual = actualDatabase.recipeQueries.selectAllRecipes().executeAsList()
        val expected = listOf(recipe.toRawRecipe())
        assertContentEquals(expected, actual)
    }

    @Test
    fun `WHEN recipe with known tags is added THEN tagLinks are added`() = runTest {
        val tag1 = Tag.Existing(id = 1L, name = "tag 1")
        val tag2 = Tag.Existing(id = 2L, name = "tag 2")
        actualDatabase.recipeQueries.addTag(tag1.name)
        actualDatabase.recipeQueries.addTag(tag2.name)

        val recipe = buildRecipe(tags = listOf(tag1, tag2))
        testDatabase.addRecipe(recipe)
        advanceUntilIdle()

        val actual = actualDatabase.recipeToTagQueries.selectAll().executeAsList()
        val expected = listOf(
            RecipeToTag(id = 1L, recipeId = recipe.id, tagId = tag1.id),
            RecipeToTag(id = 2L, recipeId = recipe.id, tagId = tag2.id)
        )
        assertContentEquals(expected, actual)
    }

    @Test
    fun `WHEN recipe with unknown tags is added THEN new tags and tagLinks are added`() = runTest {
        val tag1 = Tag.New(name = "tag 1")
        val tag2 = Tag.New(name = "tag 2")

        val recipe = buildRecipe(tags = listOf(tag1, tag2))
        testDatabase.addRecipe(recipe)
        advanceUntilIdle()

        //test for added Tags
        val actualTags = actualDatabase.tagQueries.selectAll().executeAsList()
        val expectedTags = listOf(
            RawTag(id = 1L, name = tag1.name),
            RawTag(id = 2L, name = tag2.name)
        )
        assertContentEquals(expectedTags, actualTags)

        //test for added TagLinks
        val actualRecipeToTags = actualDatabase.recipeToTagQueries.selectAll().executeAsList()
        val expectedRecipeToTags = listOf(
            RecipeToTag(id = 1L, recipeId = recipe.id, tagId = 1L),
            RecipeToTag(id = 2L, recipeId = recipe.id, tagId = 2L)
        )
        assertContentEquals(expectedRecipeToTags, actualRecipeToTags)
    }

    @Test
    fun `WHEN recipe with known ingredients is added THEN ingredientLinks are added`() = runTest {
        val ingredient1 = Ingredient.Existing(id = 1L, name = "ingredient 1", amount = 1.124, unit = IngredientUnit.None)
        val ingredient2 = Ingredient.Existing(id = 2L, name = "ingredient 2", amount = 1.254, unit = IngredientUnit.Pinch)
        actualDatabase.recipeQueries.addIngredient(ingredient1.name)
        actualDatabase.recipeQueries.addIngredient(ingredient2.name)

        val recipe = buildRecipe(ingredients = listOf(ingredient1, ingredient2))
        testDatabase.addRecipe(recipe)
        advanceUntilIdle()

        val actual = actualDatabase.recipeToIngredientQueries.selectAll().executeAsList()
        val expected = listOf(
            RecipeToIngredient(id = 1L, recipeId = recipe.id, ingredientId = ingredient1.id, amount = ingredient1.amount, unit = ingredient1.unit),
            RecipeToIngredient(id = 2L, recipeId = recipe.id, ingredientId = ingredient2.id, amount = ingredient2.amount, unit = ingredient2.unit)
        )
        assertContentEquals(expected, actual)
    }

    @Test
    fun `WHEN recipe with unknown ingredients is added THEN new ingredients and ingredientLinks are added`() = runTest {
        val ingredient1 = Ingredient.New(name = "tag 1", amount = 1.124, unit = IngredientUnit.None)
        val ingredient2 = Ingredient.New(name = "tag 2", amount = 1.254, unit = IngredientUnit.Pinch)

        val recipe = buildRecipe(ingredients = listOf(ingredient1, ingredient2))
        testDatabase.addRecipe(recipe)
        advanceUntilIdle()

        //test for added Ingredients
        val actualIngredients = actualDatabase.ingredientQueries.selectAll().executeAsList()
        val expectedIngredients = listOf(
            RawIngredient(id = 1L, name = ingredient1.name),
            RawIngredient(id = 2L, name = ingredient2.name)
        )
        assertContentEquals(expectedIngredients, actualIngredients)

        //test for added IngredientLinks
        val actualRecipeToIngredients = actualDatabase.recipeToIngredientQueries.selectAll().executeAsList()
        val expectedRecipeToIngredients = listOf(
            RecipeToIngredient(id = 1L, recipeId = recipe.id, ingredientId = 1L, amount = ingredient1.amount, unit = ingredient1.unit),
            RecipeToIngredient(id = 2L, recipeId = recipe.id, ingredientId = 2L, amount = ingredient2.amount, unit = ingredient2.unit)
        )
        assertContentEquals(expectedRecipeToIngredients, actualRecipeToIngredients)
    }

    @Test
    fun `WHEN recipe is deleted THEN recipe is deleted from database`() = runTest {
        actualDatabase.recipeQueries.addRecipe(name = "test", author = "a", rating = 0, workTimeInMinutes = 0, cookTimeInMinutes = 0, difficulty = Difficulty.NotDefined, portions = 1, text = "test")

        testDatabase.deleteRecipe(1L)
        advanceUntilIdle()

        val actualRecipes = actualDatabase.recipeQueries.selectAllRecipes().executeAsList()
        assertContentEquals(emptyList(), actualRecipes)
    }

    @Test
    fun `WHEN recipe is deleted THEN unused tags are deleted`() = runTest {
        actualDatabase.recipeQueries.addTag("should be deleted")
        actualDatabase.recipeQueries.addTag("should not be deleted")
        actualDatabase.recipeQueries.addRecipe(name = "test", author = "a", rating = 0, workTimeInMinutes = 0, cookTimeInMinutes = 0, difficulty = Difficulty.NotDefined, portions = 1, text = "test")
        actualDatabase.recipeQueries.addRecipe(name = "test", author = "a", rating = 0, workTimeInMinutes = 0, cookTimeInMinutes = 0, difficulty = Difficulty.NotDefined, portions = 1, text = "test")
        actualDatabase.recipeQueries.addTagLink(recipeId = 1L, tagId = 1L)
        actualDatabase.recipeQueries.addTagLink(recipeId = 1L, tagId = 2L)
        actualDatabase.recipeQueries.addTagLink(recipeId = 2L, tagId = 2L)

        testDatabase.deleteRecipe(1L)
        advanceUntilIdle()

        //test for tags
        val actualTags = actualDatabase.tagQueries.selectAll().executeAsList()
        val expectedTags = listOf(RawTag(id = 2L, name = "should not be deleted"))
        assertContentEquals(expectedTags, actualTags)

        //test for links
        val actualLinks = actualDatabase.recipeToTagQueries.selectAll().executeAsList()
        val expectedLinks = listOf(RecipeToTag(id = 3L, recipeId = 2L, tagId = 2L))
        assertContentEquals(expectedLinks, actualLinks)
    }

    @Test
    fun `WHEN recipe is deleted THEN unused ingredients are deleted`() = runTest {
        actualDatabase.recipeQueries.addIngredient("should be deleted")
        actualDatabase.recipeQueries.addIngredient("should not be deleted")
        actualDatabase.recipeQueries.addRecipe(name = "test", author = "a", rating = 0, workTimeInMinutes = 0, cookTimeInMinutes = 0, difficulty = Difficulty.NotDefined, portions = 1, text = "test")
        actualDatabase.recipeQueries.addRecipe(name = "test", author = "a", rating = 0, workTimeInMinutes = 0, cookTimeInMinutes = 0, difficulty = Difficulty.NotDefined, portions = 1, text = "test")
        actualDatabase.recipeQueries.addIngredientLink(recipeId = 1L, ingredientId = 1L, amount = 1.0, unit = IngredientUnit.None)
        actualDatabase.recipeQueries.addIngredientLink(recipeId = 1L, ingredientId = 2L, amount = 1.0, unit = IngredientUnit.None)
        actualDatabase.recipeQueries.addIngredientLink(recipeId = 2L, ingredientId = 2L, amount = 1.0, unit = IngredientUnit.None)

        testDatabase.deleteRecipe(1L)
        advanceUntilIdle()

        //test for ingredients
        val actualIngredients = actualDatabase.ingredientQueries.selectAll().executeAsList()
        val expectedIngredients = listOf(RawIngredient(id = 2L, name = "should not be deleted"))
        assertContentEquals(expectedIngredients, actualIngredients)

        //test for links
        val actualLinks = actualDatabase.recipeToIngredientQueries.selectAll().executeAsList()
        val expectedLinks = listOf(RecipeToIngredient(id = 3L, recipeId = 2L, ingredientId = 2L, amount = 1.0, unit = IngredientUnit.None))
        assertContentEquals(expectedLinks, actualLinks)
    }

    @Test
    fun `WHEN recipe is selected THEN a correct recipe is returned`() = runTest {
        val tag1 = Tag.Existing(id = 1L, name = "tag 1")
        val tag2 = Tag.Existing(id = 2L, name = "tag 2")
        val ingredient = Ingredient.Existing(id = 1L, name = "ingredient 1", amount = 1.0, unit = IngredientUnit.Pinch)
        val expected = buildRecipe(tags = listOf(tag1, tag2), ingredients = listOf(ingredient))
        actualDatabase.recipeQueries.addTag(tag1.name)
        actualDatabase.recipeQueries.addTag(tag2.name)
        actualDatabase.recipeQueries.addIngredient(ingredient.name)
        actualDatabase.recipeQueries.addRecipe(name = expected.name, author = expected.author, rating = expected.rating, workTimeInMinutes =  expected.workTimeInMinutes, cookTimeInMinutes = expected.cookTimeInMinutes, difficulty = expected.difficulty, portions = expected.portions, text = expected.text)
        actualDatabase.recipeQueries.addTagLink(recipeId = expected.id, tagId = tag1.id)
        actualDatabase.recipeQueries.addTagLink(recipeId = expected.id, tagId = tag2.id)
        actualDatabase.recipeQueries.addIngredientLink(recipeId = expected.id, ingredientId = ingredient.id, amount = ingredient.amount, unit = ingredient.unit)

        val actual = testDatabase.selectRecipe(expected.id)
        advanceUntilIdle()

        assertEquals(expected, actual)
    }

    @Test
    fun `WHEN recipe is updated THEN recipe is written to database`() = runTest {
        val recipe = buildRecipe()
        actualDatabase.recipeQueries.addRecipe(name = recipe.name, author = recipe.author, rating = recipe.rating, workTimeInMinutes = recipe.workTimeInMinutes, cookTimeInMinutes = recipe.cookTimeInMinutes, difficulty = recipe.difficulty, portions = recipe.portions, text = recipe.text)
        val expected = recipe.copy(
            name = "different name",
            author = "different author",
            rating = 2,
            workTimeInMinutes = 100,
            cookTimeInMinutes = 200,
            difficulty = Difficulty.Hard,
            portions = 20,
            text = "different text"
        )

        testDatabase.updateRecipe(expected)
        advanceUntilIdle()

        val actual = actualDatabase.recipeQueries.selectRecipe(1L).executeAsOne()
        assertEquals(expected.toRawRecipe(), actual)
    }

    @Test
    fun `WHEN recipe is updated THEN tags are updated`() = runTest {
        val recipe = buildRecipe()
        val tag1 = Tag.Existing(1L, "not changing")
        val tag2 = Tag.Existing(2L, "to be deleted")
        val tag3 = Tag.New("to be created")
        actualDatabase.recipeQueries.addRecipe(name = recipe.name, author = recipe.author, rating = recipe.rating, workTimeInMinutes = recipe.workTimeInMinutes, cookTimeInMinutes = recipe.cookTimeInMinutes, difficulty = recipe.difficulty, portions = recipe.portions, text = recipe.text)
        actualDatabase.recipeQueries.addTag(tag1.name)
        actualDatabase.recipeQueries.addTag(tag2.name)
        actualDatabase.recipeQueries.addTagLink(recipeId = recipe.id, tagId = tag1.id)
        actualDatabase.recipeQueries.addTagLink(recipeId = recipe.id, tagId = tag2.id)

        testDatabase.updateRecipe(recipe.copy(tags = listOf(tag1, tag3)))
        advanceUntilIdle()

        val actualTags = actualDatabase.tagQueries.selectAll().executeAsList()
        val expectedTags = listOf(RawTag(id = tag1.id, name = tag1.name), RawTag(id = 3L, name = tag3.name))
        assertContentEquals(expectedTags, actualTags)

        val actualLinks = actualDatabase.recipeToTagQueries.selectAll().executeAsList()
        val expectedLinks = listOf(RecipeToTag(id = 1L, recipeId = recipe.id, tagId = tag1.id), RecipeToTag(id = 2L, recipeId = recipe.id, tagId = 3L))
        assertContentEquals(expectedLinks, actualLinks)
    }

    @Test
    fun `WHEN recipe is updated THEN ingredients are updated`() = runTest {
        val recipe = buildRecipe()
        val ingredient1 = Ingredient.Existing(id = 1L, name = "not changing", amount = 1.5, unit = IngredientUnit.TableSpoon)
        val ingredient2 = Ingredient.Existing(id = 2L, name = "to be deleted", amount = 22.3, unit = IngredientUnit.Length)
        val ingredient3 = Ingredient.New(name = "to be created", amount = 5.7, unit = IngredientUnit.Volume)
        actualDatabase.recipeQueries.addRecipe(name = recipe.name, author = recipe.author, rating = recipe.rating, workTimeInMinutes = recipe.workTimeInMinutes, cookTimeInMinutes = recipe.cookTimeInMinutes, difficulty = recipe.difficulty, portions = recipe.portions, text = recipe.text)
        actualDatabase.recipeQueries.addIngredient(ingredient1.name)
        actualDatabase.recipeQueries.addIngredient(ingredient2.name)
        actualDatabase.recipeQueries.addIngredientLink(recipeId = recipe.id, ingredientId = ingredient1.id, amount = ingredient1.amount, unit = ingredient1.unit)
        actualDatabase.recipeQueries.addIngredientLink(recipeId = recipe.id, ingredientId = ingredient2.id, amount = ingredient2.amount, unit = ingredient2.unit)

        testDatabase.updateRecipe(recipe.copy(ingredients = listOf(ingredient1, ingredient3)))
        advanceUntilIdle()

        val actualIngredients = actualDatabase.ingredientQueries.selectAll().executeAsList()
        val expectedIngredients = listOf(RawIngredient(id = ingredient1.id, name = ingredient1.name), RawIngredient(id = 3L, name = ingredient3.name))
        assertContentEquals(expectedIngredients, actualIngredients)

        val actualLinks = actualDatabase.recipeToIngredientQueries.selectAll().executeAsList()
        val expectedLinks = listOf(RecipeToIngredient(id = 1L, recipeId = recipe.id, ingredientId = ingredient1.id, amount = ingredient1.amount, unit = ingredient1.unit), RecipeToIngredient(id = 2L, recipeId = recipe.id, ingredientId = 3L, amount = ingredient3.amount, unit = ingredient3.unit))
        assertContentEquals(expectedLinks, actualLinks)
    }

    @Test
    fun `WHEN recipe is searched THEN only recipes with matching criteria are returned`() = runTest {
        val nameSearch = "search term"
        val authorSearch = "test author"
        val ratingSearch = 3
        val maxTimeSearch = 40
        val difficultySearch = Difficulty.Hard
        val filterTagsSearch = listOf(Tag.Existing(1L, "tag1"), Tag.Existing(2L, "tag2"))
        val filterIngredientsSearch = listOf(Ingredient.Existing(1L, "ingredient", 1.0, IngredientUnit.None))
        val notSearchedRecipe = buildRecipe(tags = filterTagsSearch, ingredients = filterIngredientsSearch)
        val searchedRecipe = Recipe(id = 2L, name = nameSearch, author = authorSearch, rating = ratingSearch + 1, workTimeInMinutes = 20, cookTimeInMinutes = 20, difficulty = difficultySearch, portions = 1, text = "test", tags = filterTagsSearch, ingredients = filterIngredientsSearch)

        actualDatabase.recipeQueries.addRecipe(name = notSearchedRecipe.name, author = notSearchedRecipe.author, rating = notSearchedRecipe.rating, workTimeInMinutes = notSearchedRecipe.workTimeInMinutes, cookTimeInMinutes = notSearchedRecipe.cookTimeInMinutes, difficulty = notSearchedRecipe.difficulty, portions = notSearchedRecipe.portions, text = notSearchedRecipe.text)
        actualDatabase.recipeQueries.addRecipe(name = searchedRecipe.name, author = searchedRecipe.author, rating = searchedRecipe.rating, workTimeInMinutes = searchedRecipe.workTimeInMinutes, cookTimeInMinutes = searchedRecipe.cookTimeInMinutes, difficulty = searchedRecipe.difficulty, portions = searchedRecipe.portions, text = searchedRecipe.text)
        actualDatabase.recipeQueries.addTag(filterTagsSearch[0].name)
        actualDatabase.recipeQueries.addTag(filterTagsSearch[1].name)
        actualDatabase.recipeQueries.addIngredient(filterIngredientsSearch[0].name)
        actualDatabase.recipeQueries.addTagLink(recipeId = notSearchedRecipe.id, tagId = filterTagsSearch[0].id)
        actualDatabase.recipeQueries.addTagLink(recipeId = notSearchedRecipe.id, tagId = filterTagsSearch[1].id)
        actualDatabase.recipeQueries.addTagLink(recipeId = searchedRecipe.id, tagId = filterTagsSearch[0].id)
        actualDatabase.recipeQueries.addTagLink(recipeId = searchedRecipe.id, tagId = filterTagsSearch[1].id)
        actualDatabase.recipeQueries.addIngredientLink(recipeId = notSearchedRecipe.id, ingredientId = filterIngredientsSearch[0].id, amount = filterIngredientsSearch[0].amount, unit = filterIngredientsSearch[0].unit)
        actualDatabase.recipeQueries.addIngredientLink(recipeId = searchedRecipe.id, ingredientId = filterIngredientsSearch[0].id, amount = filterIngredientsSearch[0].amount, unit = filterIngredientsSearch[0].unit)

        val actual = testDatabase.searchRecipe(name = nameSearch, author = authorSearch, rating = ratingSearch, maxTime = maxTimeSearch, difficulty = difficultySearch, tags = filterTagsSearch, ingredients = filterIngredientsSearch)
        advanceUntilIdle()

        val expected = listOf(RecipePreview(id = searchedRecipe.id, name = searchedRecipe.name, author = searchedRecipe.author, rating = searchedRecipe.rating, difficulty = searchedRecipe.difficulty, time = searchedRecipe.cookTimeInMinutes + searchedRecipe.workTimeInMinutes, tags = filterTagsSearch))
        assertContentEquals(expected, actual)
    }

    @Test
    fun `WHEN ingredients change THEN the change is observed`() = runTest {
        val actual = mutableListOf<List<Ingredient.Existing>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testDatabase.ingredients(testScheduler).toList(actual)
        }

        val ingredient1 = Ingredient.Existing(1L, "ingredient1", amount = 0.0, unit = IngredientUnit.None)
        actualDatabase.recipeQueries.addIngredient(ingredient1.name)
        val ingredient2 = Ingredient.Existing(2L, "ingredient2", amount = 0.0, unit = IngredientUnit.None)
        actualDatabase.recipeQueries.addIngredient(ingredient2.name)
        actualDatabase.recipeQueries.removeUnusedIngredients()

        val expected = listOf(emptyList(), listOf(ingredient1), listOf(ingredient1, ingredient2), emptyList())
        assertContentEquals(expected, actual)
    }

    @Test
    fun `WHEN tags change THEN the change is observed`() = runTest {
        val actual = mutableListOf<List<Tag.Existing>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testDatabase.tags(testScheduler).toList(actual)
        }

        val tag1 = Tag.Existing(1L, "tag1")
        actualDatabase.recipeQueries.addTag(tag1.name)
        val tag2 = Tag.Existing(2L, "tag2")
        actualDatabase.recipeQueries.addTag(tag2.name)
        actualDatabase.recipeQueries.removeUnusedTags()

        val expected = listOf(emptyList(), listOf(tag1), listOf(tag1, tag2), emptyList())
        assertContentEquals(expected, actual)
    }
}

expect fun createDriver() : SqlDriver

private fun buildRecipe(
    id: Long = 1L,
    tags: List<Tag> = emptyList(),
    ingredients: List<Ingredient> = emptyList()
) = Recipe(
    id = id,
    name = "test recipe",
    author = "test",
    rating = 0,
    workTimeInMinutes = 0,
    cookTimeInMinutes = 0,
    difficulty = Difficulty.NotDefined,
    portions = 1,
    text = "test text",
    tags = tags,
    ingredients = ingredients
)

private fun Recipe.toRawRecipe() =
    RawRecipe(
        id = id,
        name = name,
        author = author,
        rating = rating,
        workTimeInMinutes = workTimeInMinutes,
        cookTimeInMinutes = cookTimeInMinutes,
        difficulty= difficulty,
        portions = portions,
        text = text
    )
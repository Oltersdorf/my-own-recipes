package com.olt.mor.common.database.data

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class IngredientUnitTest {
    private val adapter = IngredientUnit.Adapter()

    private val encodeData = listOf(
        IngredientUnit.Volume to "v",
        IngredientUnit.Weight to "w",
        IngredientUnit.Length to "l",
        IngredientUnit.Piece to "pc",
        IngredientUnit.TeaSpoon to "teas",
        IngredientUnit.TableSpoon to "tabs",
        IngredientUnit.Pinch to "pn",
        IngredientUnit.None to ""
    )

    private val decodeToNoneData = listOf(
        "",
        " ",
        "v ",
        " v",
        "abc"
    )

    @Test
    fun `WHEN I decode 'v' THEN I get Volume`() {
        assertIs<IngredientUnit.Volume>(adapter.decode("v"))
    }

    @Test
    fun `WHEN I decode 'w' THEN I get Weight`() {
        assertIs<IngredientUnit.Weight>(adapter.decode("w"))
    }

    @Test
    fun `WHEN I decode 'l' THEN I get Length`() {
        assertIs<IngredientUnit.Length>(adapter.decode("l"))
    }

    @Test
    fun `WHEN I decode 'pc' THEN I get Piece`() {
        assertIs<IngredientUnit.Piece>(adapter.decode("pc"))
    }

    @Test
    fun `WHEN I decode 'teas' THEN I get TeaSpoon`() {
        assertIs<IngredientUnit.TeaSpoon>(adapter.decode("teas"))
    }

    @Test
    fun `WHEN I decode 'tabs' THEN I get TableSpoon`() {
        assertIs<IngredientUnit.TableSpoon>(adapter.decode("tabs"))
    }

    @Test
    fun `WHEN I decode 'pn' THEN I get Pinch`() {
        assertIs<IngredientUnit.Pinch>(adapter.decode("pn"))
    }

    @TestFactory
    fun `decode to None`() = decodeToNoneData
        .map { input ->
            DynamicTest.dynamicTest("WHEN I decode \"$input\" THEN I get None") {
                assertIs<IngredientUnit.None>(adapter.decode(input))
            }
        }

    @TestFactory
    fun encode() = encodeData
        .map { (input, expected) ->
            DynamicTest.dynamicTest("WHEN I encode ${input::class.simpleName} THEN I get \"$expected\"") {
                assertEquals(expected, adapter.encode(input))
            }
        }
}
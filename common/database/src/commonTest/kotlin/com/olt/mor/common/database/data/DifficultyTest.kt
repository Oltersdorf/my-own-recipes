package com.olt.mor.common.database.data

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DifficultyTest {
    private val adapter = Difficulty.Adapter()

    private val encodeData = listOf(
        Difficulty.NotDefined to 0L,
        Difficulty.Easy to 1L,
        Difficulty.Medium to 2L,
        Difficulty.Hard to 3L
    )

    private val decodeToNotDefinedData = listOf(
        Long.MIN_VALUE,
        -10L,
        0L,
        4L,
        10L,
        Long.MAX_VALUE
    )

    @Test
    fun `WHEN I decode 1L THEN I get Easy`() {
        assertIs<Difficulty.Easy>(adapter.decode(1L))
    }

    @Test
    fun `WHEN I decode 2L THEN I get Medium`() {
        assertIs<Difficulty.Medium>(adapter.decode(2L))
    }

    @Test
    fun `WHEN I decode 3L THEN I get Hard`() {
        assertIs<Difficulty.Hard>(adapter.decode(3L))
    }

    @TestFactory
    fun `decode to NotDefined`() = decodeToNotDefinedData
        .map { input ->
            DynamicTest.dynamicTest("WHEN I decode $input THEN I get NotDefined") {
                assertIs<Difficulty.NotDefined>(adapter.decode(input))
            }
        }

    @TestFactory
    fun encode() = encodeData
        .map { (input, expected) ->
            DynamicTest.dynamicTest("WHEN I encode ${input::class.simpleName} THEN I get $expected") {
                assertEquals(expected, adapter.encode(input))
            }
        }
}
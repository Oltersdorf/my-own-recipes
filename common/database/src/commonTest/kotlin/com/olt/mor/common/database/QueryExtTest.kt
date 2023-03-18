package com.olt.mor.common.database

import com.badoo.reaktive.observable.map
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.assertValues
import com.badoo.reaktive.test.observable.test
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.db.SqlCursor
import kotlin.test.Test

class QueryExtTest {
    class TestQuery(var value: Long) : Query<Long>(queries = mutableListOf(), mapper = { it.getLong(0)!! }) {

        override fun execute(): SqlCursor = object : SqlCursor {
            override fun close() {}

            override fun getBytes(index: Int): ByteArray? = null

            override fun getDouble(index: Int): Double? = null

            override fun getLong(index: Int): Long = value

            override fun getString(index: Int): String? = null

            override fun next(): Boolean = false
        }
    }

    @Test
    fun `WHEN called THEN first value is emitted`() {
        val expected = 0L

        val testQuery = TestQuery(value = expected)
        val observable = testQuery.observed().map {
            it.mapper(it.execute())
        }.test()

        observable.assertValue(expected)
    }

    @Test
    fun `WHEN query result change THEN observable is updated`() {
        val firstExpected = 0L
        val otherExpected = listOf(3L, -5L, 10L, 100L, Long.MAX_VALUE, Long.MIN_VALUE)
        val expected = listOf(firstExpected, *otherExpected.toTypedArray())

        val testQuery = TestQuery(firstExpected)
        val observable = testQuery.observed().map {
            it.mapper(it.execute())
        }.test()

        for (i in otherExpected) {
            testQuery.value = i
            testQuery.notifyDataChanged()
        }

        observable.assertValues(expected)
    }

    @Test
    fun `WHEN canceled THEN unsubscribed`() {
        val expected = 0L

        val testQuery = TestQuery(expected)
        val observable = testQuery.observed().map {
            it.mapper(it.execute())
        }.test()

        observable.dispose()

        testQuery.value = 50L
        testQuery.notifyDataChanged()

        observable.assertValue(expected)
    }
}
package com.olt.mor.common.search.integration

import com.olt.mor.database.MyOwnRecipes
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import java.util.*

actual fun testDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, properties = Properties(1).apply { put("foreign_keys", "true") })
    MyOwnRecipes.Schema.create(driver)
    return driver
}
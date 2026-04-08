package org.simpleapps.saveable.data.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {

    fun create(): Database {
        val databasePath = getDatabasePath()

        val db = Database.connect(
            url    = "jdbc:sqlite:$databasePath",
            driver = "org.sqlite.JDBC"
        )

        // Create tables if they don't exist yet
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(
                CategoryTable,
                ItemTable
            )
            seedBuiltInCategories()
        }

        return db
    }

    private fun getDatabasePath(): String {
        val appDataPath = System.getenv("APPDATA") ?: System.getProperty("user.home")
        val appDir = File(appDataPath, "SaveableApp")
        appDir.mkdirs()

        return appDir.resolve("saveable.db").absolutePath
    }

    private fun seedBuiltInCategories() {
        val existing = CategoryTable
            .select(CategoryTable.name)
            .map { it[CategoryTable.name] }

        val defaults = listOf(
            "notes",
            "links",
            "passwords",
        )

        defaults
            .filter { name -> name !in existing }
            .forEach { name ->
                CategoryTable.insert {
                    it[CategoryTable.name] = name
                }
            }
    }
}
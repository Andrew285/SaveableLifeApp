package org.simpleapps.saveable.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.simpleapps.saveable.data.db.CategoryTable
import org.simpleapps.saveable.data.db.ItemTable
import org.simpleapps.saveable.data.db.toCategory
import org.simpleapps.saveable.data.db.toSaveableItem
import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.domain.category.Category
import org.simpleapps.saveable.domain.repository.IMainRepository
import org.simpleapps.saveable.util.logger

class MainRepositoryImpl(
    private val db: Database
): IMainRepository {
    private val log = logger()

    override suspend fun addItem(categoryName: String, content: String) = withContext(Dispatchers.IO) {
        log.debug("Adding item to category=$categoryName in repository")
        transaction(db) {
            ItemTable.insert {
                it[ItemTable.categoryName] = categoryName
                it[ItemTable.content] = content
                it[ItemTable.createdAt] = System.currentTimeMillis()
            }
        }
        log.info("Item added successfully to $categoryName from repository")
    }

    override suspend fun getItemsByCategory(categoryName: String): List<SaveableItem> = withContext(Dispatchers.IO) {
        log.debug("Getting items by category in repository=$categoryName")
        val result = transaction(db) {
            ItemTable
                .selectAll()
                .where { ItemTable.categoryName eq categoryName }
                .orderBy(ItemTable.createdAt to SortOrder.DESC)
                .map { it.toSaveableItem() }
        }
        log.debug("Got items by category from repository=$categoryName")
        return@withContext result
    }

    override suspend fun getAllCategories(): List<Category> {
        return transaction(db) {
            CategoryTable
                .selectAll()
                .map { it.toCategory() }
        }
    }
}
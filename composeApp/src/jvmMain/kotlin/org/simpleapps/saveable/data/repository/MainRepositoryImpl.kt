package org.simpleapps.saveable.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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
) : IMainRepository {
    private val log = logger()

    override suspend fun addItem(
        categoryName: String,
        content     : String,
        imageBase64 : String?
    ) = withContext(Dispatchers.IO) {
        log.debug("Adding item to category=$categoryName, hasImage=${imageBase64 != null}")
        transaction(db) {
            ItemTable.insert {
                it[ItemTable.categoryName] = categoryName
                it[ItemTable.content]      = content
                it[ItemTable.createdAt]    = System.currentTimeMillis()
                it[ItemTable.imageBase64]  = imageBase64
            }
        }
        log.info("Item added successfully to $categoryName")
    }

    override suspend fun editItem(id: Long, content: String) = withContext(Dispatchers.IO) {
        transaction(db) {
            ItemTable.update({ ItemTable.id eq id }) {
                it[ItemTable.content] = content
            }
        }
    }

    override suspend fun deleteItem(id: Long) = withContext(Dispatchers.IO) {
        transaction(db) {
            ItemTable.deleteWhere { ItemTable.id eq id }
        }
    }

    override suspend fun addCategory(categoryName: String) {
        transaction(db) {
            CategoryTable.insert {
                it[CategoryTable.name] = categoryName
            }
        }
    }

    override suspend fun getItemsByCategory(categoryName: String): List<SaveableItem> =
        withContext(Dispatchers.IO) {
            log.debug("Getting items by category=$categoryName")
            transaction(db) {
                ItemTable
                    .selectAll()
                    .where { ItemTable.categoryName eq categoryName }
                    .orderBy(ItemTable.createdAt to SortOrder.DESC)
                    .map { it.toSaveableItem() }
            }
        }

    override suspend fun getAllCategories(): List<Category> =
        transaction(db) {
            CategoryTable.selectAll().map { it.toCategory() }
        }
}
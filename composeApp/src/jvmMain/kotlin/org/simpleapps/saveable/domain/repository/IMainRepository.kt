package org.simpleapps.saveable.domain.repository

import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.domain.category.Category

interface IMainRepository {
    suspend fun addItem(categoryName: String, content: String, imageBase64: String? = null)
    suspend fun editItem(id: Long, content: String): Int
    suspend fun deleteItem(id: Long): Int
    suspend fun addCategory(categoryName: String)
    suspend fun getItemsByCategory(categoryName: String): List<SaveableItem>
    suspend fun getAllCategories(): List<Category>
}
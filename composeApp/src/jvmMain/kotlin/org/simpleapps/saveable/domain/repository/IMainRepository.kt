package org.simpleapps.saveable.domain.repository

import org.simpleapps.saveable.domain.SaveableItem

interface IMainRepository {
    suspend fun addItem(categoryName: String, content: String)
    suspend fun getItemsByCategory(categoryName: String): List<SaveableItem>
}
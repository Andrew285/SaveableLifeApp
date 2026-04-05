package org.simpleapps.saveable.domain

import org.simpleapps.saveable.domain.category.Category
import java.util.Date

data class SaveableItem(
    val id: Long = 0,
    val content: String,
    val categoryName: String,
    val createdAt: Long = System.currentTimeMillis()
)
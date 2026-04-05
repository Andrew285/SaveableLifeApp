package org.simpleapps.saveable.domain

data class SaveableItem(
    val id: Long = 0,
    val content: String,
    val categoryName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val imageBase64  : String? = null
)
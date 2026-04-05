package org.simpleapps.saveable.data.db

import org.jetbrains.exposed.sql.ResultRow
import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.domain.category.Category

fun ResultRow.toSaveableItem() = SaveableItem(
    id = this[ItemTable.id],
    content = this[ItemTable.content],
    categoryName = this[ItemTable.categoryName],
    createdAt = this[ItemTable.createdAt],
    imageBase64  = this[ItemTable.imageBase64],
)

fun ResultRow.toCategory() = Category(
    id = this[CategoryTable.id],
    name = this[CategoryTable.name],
)
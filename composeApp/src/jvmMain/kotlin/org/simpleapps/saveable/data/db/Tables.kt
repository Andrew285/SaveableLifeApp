package org.simpleapps.saveable.data.db

import org.jetbrains.exposed.sql.Table

object CategoryTable: Table("categories") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 64).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}

object ItemTable: Table("items") {
    val id = long("id").autoIncrement()
    val content = text("content")
    val categoryName = varchar("category_name", 64)
        .references(CategoryTable.name)
    val createdAt = long("created_at")
    val imageBase64  = text("image_base64").nullable()

    override val primaryKey = PrimaryKey(id)
}
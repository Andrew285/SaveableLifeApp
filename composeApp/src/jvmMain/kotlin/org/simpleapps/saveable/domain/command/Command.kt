package org.simpleapps.saveable.domain.command

sealed class Command {
    data class AddItem(
        val categoryName: String,
        val content     : String,
        val imageBase64 : String? = null   // non-null when pasting an image
    ) : Command()

    data class EditItem(val id: Long, val content: String) : Command()
    data class DeleteItem(val id: Long) : Command()
    data class AddCategory(val categoryName: String) : Command()
    data class List(val categoryName: String) : Command()
    data object Clear : Command()
    data class Translate(val text: String): Command()
}
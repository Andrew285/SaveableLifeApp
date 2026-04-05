package org.simpleapps.saveable.domain.command

sealed class Command {
    data class AddItem(val categoryName: String, val content: String): Command()
    data class EditItem(val id: Long, val content: String): Command()
    data class DeleteItem(val id: Long): Command()
    data class AddCategory(val categoryName: String): Command()
//    data class EditCategory(val id: Long, val categoryName: String): Command()
//    data class DeleteCategory(val index: Int): Command()
    data class List(val categoryName: String): Command()
    data object Clear: Command()
}

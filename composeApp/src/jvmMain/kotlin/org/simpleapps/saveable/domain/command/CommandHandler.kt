package org.simpleapps.saveable.domain.command

import org.simpleapps.saveable.domain.usecases.AddCategoryUseCase
import org.simpleapps.saveable.domain.usecases.AddItemUseCase
import org.simpleapps.saveable.domain.usecases.DeleteItemUseCase
import org.simpleapps.saveable.domain.usecases.EditItemUseCase
import org.simpleapps.saveable.domain.usecases.GetListByCategoryUseCase

class CommandHandler(
    private val addItemUseCase         : AddItemUseCase,
    private val editItemUseCase        : EditItemUseCase,
    private val deleteItemUseCase      : DeleteItemUseCase,
    private val addCategoryUseCase     : AddCategoryUseCase,
    private val getListByCategoryUseCase: GetListByCategoryUseCase,
) {
    suspend fun handle(command: Command): CommandResult {
        return try {
            when (command) {
                is Command.AddItem -> {
                    addItemUseCase(command.categoryName, command.content, command.imageBase64)
                    CommandResult.Success("Item is added successfully")
                }
                is Command.EditItem -> {
                    editItemUseCase(command.id, command.content)
                    CommandResult.Success("Item is updated successfully")
                }
                is Command.DeleteItem -> {
                    deleteItemUseCase(command.id)
                    CommandResult.Success("Item is deleted successfully")
                }
                is Command.AddCategory -> {
                    addCategoryUseCase(command.categoryName)
                    CommandResult.Success("Category is added successfully")
                }
                is Command.List -> {
                    val data = getListByCategoryUseCase(command.categoryName)
                    CommandResult.ItemsList(data)
                }
                is Command.Clear -> CommandResult.ItemsCleared
            }
        } catch (e: Exception) {
            CommandResult.Error(e.message)
        }
    }
}
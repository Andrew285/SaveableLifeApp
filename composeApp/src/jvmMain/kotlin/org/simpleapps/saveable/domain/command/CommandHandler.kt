package org.simpleapps.saveable.domain.command

import org.simpleapps.saveable.domain.usecases.AddCommandUseCase
import org.simpleapps.saveable.domain.usecases.GetListByCategoryUseCase

class CommandHandler(
    private val addItemUseCase: AddCommandUseCase,
    private val getListByCategoryUseCase: GetListByCategoryUseCase,
) {

    suspend fun handle(command: Command): CommandResult {
        return try {
            return when (command) {
                is Command.Add -> {
                    addItemUseCase(command.categoryName, command.content)
                    CommandResult.Success("Item is added successfully")
                }
                is Command.List -> {
                    val data = getListByCategoryUseCase(command.categoryName)
                    CommandResult.ItemsList(data)
                }
            }
        } catch (e: Exception) {
            CommandResult.Error(e.message)
        }
    }
}
package org.simpleapps.saveable.domain.command

import org.simpleapps.saveable.domain.SaveableItem

sealed class CommandResult {
    data class ItemsList(val data: List<SaveableItem>): CommandResult()
    data class Success(val message: String): CommandResult()
    data class Error(val errorMessage: String?): CommandResult()
    data object ItemsCleared: CommandResult()
}
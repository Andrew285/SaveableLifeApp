package org.simpleapps.saveable.domain.command

import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.domain.translation.TranslationResult

sealed class CommandResult {
    data class Success(val message: String)                   : CommandResult()
    data class ItemsList(val data: List<SaveableItem>)        : CommandResult()
    data class TranslationData(val result: TranslationResult) : CommandResult()
    data object ItemsCleared                                  : CommandResult()
    data class Error(val errorMessage: String?)               : CommandResult()
}
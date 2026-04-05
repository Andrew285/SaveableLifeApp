package org.simpleapps.saveable.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext.get
import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.domain.autocomplete.AutocompleteEngine
import org.simpleapps.saveable.domain.autocomplete.Suggestion
import org.simpleapps.saveable.domain.category.Category
import org.simpleapps.saveable.domain.command.Command
import org.simpleapps.saveable.domain.command.CommandHandler
import org.simpleapps.saveable.domain.command.CommandParser
import org.simpleapps.saveable.domain.command.CommandResult
import org.simpleapps.saveable.domain.usecases.GetCategoriesUseCase
import org.simpleapps.saveable.util.logger

data class MainUiState(
    val inputText: String = "",
    val categories: List<String> = emptyList(),
    val items: List<SaveableItem> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    val isLoading: Boolean = false,
    val suggestions: List<Suggestion> = emptyList(),
    val selectedSuggestion: Int = -1
)

class MainStateHolder(
    private val commandParser  : CommandParser,
    private val commandHandler : CommandHandler,
    private val getCategoriesUseCase: GetCategoriesUseCase
) {
    var uiState by mutableStateOf(MainUiState()); private set
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val log = logger()
    private val autocompleteEngine = AutocompleteEngine()

    init {
        loadCategories()
    }
    fun loadCategories() {
        scope.launch {
            val cats = getCategoriesUseCase()
            uiState  = uiState.copy(categories = cats.map { it.name })
        }
    }

    fun onInputChanged(text: String) {
        val suggestions = autocompleteEngine.suggest(text, uiState.categories)
        uiState = uiState.copy(
            inputText         = text,
            suggestions       = suggestions,
            selectedSuggestion = -1,
            successMessage     = if (text.isNotEmpty()) "" else uiState.successMessage,
            isError            = if (text.isNotEmpty()) false else uiState.isError,
            errorMessage       = if (text.isNotEmpty()) "" else uiState.errorMessage
        )
    }

    // called when user presses ↑ / ↓ in the input field
    fun onSuggestionNavigate(down: Boolean) {
        val size = uiState.suggestions.size
        if (size == 0) return
        val current = uiState.selectedSuggestion
        val next    = when {
            down && current < size - 1 -> current + 1
            !down && current > 0       -> current - 1
            !down && current == 0      -> -1
            else                       -> current
        }
        uiState = uiState.copy(selectedSuggestion = next)
    }

    // called when user clicks a suggestion or presses Tab
    fun onSuggestionSelected(suggestion: Suggestion) {
        uiState = uiState.copy(
            inputText         = suggestion.insertText,
            suggestions       = emptyList(),
            selectedSuggestion = -1
        )
    }

    fun onSubmit() {
        // if a suggestion is highlighted, select it instead of submitting
        val selected = uiState.selectedSuggestion
        if (selected >= 0 && selected < uiState.suggestions.size) {
            onSuggestionSelected(uiState.suggestions[selected])
            return
        }

        scope.launch {
            log.debug("onSubmit called with input: ${uiState.inputText}")

            uiState = uiState.copy(
                isLoading = true
            )

            val command = commandParser.parse(uiState.inputText) ?: run {
                log.warn("Failed to parse input: ${uiState.inputText}")
                uiState = uiState.copy(
                    isError = true,
                    errorMessage = "Unknown input, try again",
                    isLoading = false
                )
                return@launch
            }

            log.info("Executing command: $command")
            val result = commandHandler.handle(command)

            handleCommandResult(result)
        }
    }

    fun handleCommandResult(result: CommandResult) {
        when (result) {
            is CommandResult.Success -> {
                log.info("Command succeeded: ${result.message}")
                loadCategories()
                uiState = uiState.copy(
                    isError = false,
                    isLoading = false,
                    successMessage = result.message,
                    inputText = ""
                )
            }
            is CommandResult.ItemsList -> {
                log.debug("Got ${result.data.size} items")
                uiState = uiState.copy(
                    isError = false,
                    isLoading = false,
                    items = result.data,
                    inputText = ""
                )
            }
            is CommandResult.ItemsCleared -> {
                uiState = uiState.copy(
                    isError = false,
                    isLoading = false,
                    items = emptyList(),
                    inputText = ""
                )
            }
            is CommandResult.Error -> {
                log.error("Command failed: ${result.errorMessage}")
                uiState = uiState.copy(
                    isError = true,
                    errorMessage = result.errorMessage.toString(),
                    isLoading = false,
                )
            }
        }
    }

    fun onEditItem(item: SaveableItem, newContent: String) {
        scope.launch {
            val result = commandHandler.handle(Command.EditItem(item.id, newContent))
            when (result) {
                is CommandResult.Success -> {
                    // optimistically update the item in the list
                    uiState = uiState.copy(
                        items = uiState.items.map {
                            if (it.id == item.id) it.copy(content = newContent) else it
                        },
                        successMessage = "Item is updated successfully"
                    )
                }
                is CommandResult.Error -> {
                    uiState = uiState.copy(
                        isError      = true,
                        errorMessage = "Failed to edit: ${result.errorMessage}"
                    )
                }
                else -> Unit
            }
        }
    }

    fun onDeleteItem(item: SaveableItem) {
        scope.launch {
            val result = commandHandler.handle(Command.DeleteItem(item.id))
            when (result) {
                is CommandResult.Success -> {
                    uiState = uiState.copy(
                        items = uiState.items.filter { it.id != item.id },
                        successMessage = "Item is deleted successfully"
                    )
                }
                is CommandResult.Error -> {
                    uiState = uiState.copy(
                        isError      = true,
                        errorMessage = "Failed to delete: ${result.errorMessage}"
                    )
                }
                else -> Unit
            }
        }
    }

    fun onDispose() = scope.cancel()
}
package org.simpleapps.saveable.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.domain.autocomplete.AutocompleteEngine
import org.simpleapps.saveable.domain.autocomplete.Suggestion
import org.simpleapps.saveable.domain.command.Command
import org.simpleapps.saveable.domain.command.CommandHandler
import org.simpleapps.saveable.domain.command.CommandParser
import org.simpleapps.saveable.domain.command.CommandResult
import org.simpleapps.saveable.domain.usecases.GetCategoriesUseCase
import org.simpleapps.saveable.util.logger
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO

data class MainUiState(
    val inputText         : String          = "",
    val categories        : List<String>    = emptyList(),
    val items             : List<SaveableItem> = emptyList(),
    val isError           : Boolean         = false,
    val errorMessage      : String          = "",
    val successMessage    : String          = "",
    val isLoading         : Boolean         = false,
    val suggestions       : List<Suggestion> = emptyList(),
    val selectedSuggestion: Int             = -1,
    /** Non-null while user has pasted an image and hasn't submitted yet */
    val pendingImageBase64: String?         = null,
    /** Preview label shown in the input bar */
    val pendingImageLabel : String          = ""
)

class MainStateHolder(
    private val commandParser          : CommandParser,
    private val commandHandler         : CommandHandler,
    private val getCategoriesUseCase   : GetCategoriesUseCase
) {
    var uiState by mutableStateOf(MainUiState()); private set
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val log   = logger()
    private val autocompleteEngine = AutocompleteEngine()

    init { loadCategories() }

    fun loadCategories() {
        scope.launch {
            val cats = getCategoriesUseCase()
            uiState  = uiState.copy(categories = cats.map { it.name })
        }
    }

    fun onInputChanged(text: String) {
        val suggestions = autocompleteEngine.suggest(text, uiState.categories)
        uiState = uiState.copy(
            inputText          = text,
            suggestions        = suggestions,
            selectedSuggestion = -1,
            successMessage     = if (text.isNotEmpty()) "" else uiState.successMessage,
            isError            = if (text.isNotEmpty()) false else uiState.isError,
            errorMessage       = if (text.isNotEmpty()) "" else uiState.errorMessage
        )
    }

    fun onSuggestionNavigate(down: Boolean) {
        val size = uiState.suggestions.size
        if (size == 0) return
        val current = uiState.selectedSuggestion
        val next = when {
            down  && current < size - 1 -> current + 1
            !down && current > 0        -> current - 1
            !down && current == 0       -> -1
            else                        -> current
        }
        uiState = uiState.copy(selectedSuggestion = next)
    }

    fun onSuggestionSelected(suggestion: Suggestion) {
        uiState = uiState.copy(
            inputText          = suggestion.insertText,
            suggestions        = emptyList(),
            selectedSuggestion = -1
        )
    }

    /**
     * Called from the UI when a paste event fires.
     * Checks the system clipboard for an image; if found stores it as a pending Base64 blob
     * so the next /add submit will attach it to the item.
     *
     * @return true if an image was captured from the clipboard.
     */
    fun onPasteEvent(): Boolean {
        return try {
            val clipboard   = Toolkit.getDefaultToolkit().systemClipboard
            val transferable = clipboard.getContents(null) ?: return false
            if (!transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) return false

            val image = transferable.getTransferData(DataFlavor.imageFlavor) as? BufferedImage
                ?: return false

            val baos = ByteArrayOutputStream()
            ImageIO.write(image, "PNG", baos)
            val base64 = Base64.getEncoder().encodeToString(baos.toByteArray())

            uiState = uiState.copy(
                pendingImageBase64 = base64,
                pendingImageLabel  = "📎 image ready to attach"
            )
            true
        } catch (e: Exception) {
            log.warn("Clipboard image read failed: ${e.message}")
            false
        }
    }

    fun onClearPendingImage() {
        uiState = uiState.copy(pendingImageBase64 = null, pendingImageLabel = "")
    }

    fun onSubmit() {
        val selected = uiState.selectedSuggestion
        if (selected >= 0 && selected < uiState.suggestions.size) {
            onSuggestionSelected(uiState.suggestions[selected])
            return
        }

        scope.launch {
            uiState = uiState.copy(isLoading = true)

            val pendingImage = uiState.pendingImageBase64

            // If there is a pending image but the user typed a bare category (no /add prefix),
            // auto-wrap it: treat the whole input as the category, content = ""
            val rawInput = uiState.inputText.trim()

            val command: Command? = if (pendingImage != null) {
                // Try normal parse first; if null, build AddItem directly from input
                val parsed = commandParser.parse(rawInput)
                when {
                    parsed is Command.AddItem -> parsed.copy(imageBase64 = pendingImage)
                    parsed == null && rawInput.isNotEmpty() ->
                        // "/add <category>" or just "<category>" – accept both
                        if (rawInput.startsWith("/add ") && rawInput.split(" ").size == 2) {
                            Command.AddItem(rawInput.split(" ")[1], "", pendingImage)
                        } else null
                    else -> parsed
                }
            } else {
                commandParser.parse(rawInput)
            }

            if (command == null) {
                uiState = uiState.copy(
                    isError      = true,
                    errorMessage = "Unknown input, try again",
                    isLoading    = false
                )
                return@launch
            }

            val result = commandHandler.handle(command)
            // Clear pending image after any submit attempt
            uiState = uiState.copy(pendingImageBase64 = null, pendingImageLabel = "")
            handleCommandResult(result)
        }
    }

    fun handleCommandResult(result: CommandResult) {
        when (result) {
            is CommandResult.Success -> {
                loadCategories()
                uiState = uiState.copy(
                    isError        = false,
                    isLoading      = false,
                    successMessage = result.message,
                    inputText      = ""
                )
            }
            is CommandResult.ItemsList -> {
                uiState = uiState.copy(
                    isError   = false,
                    isLoading = false,
                    items     = result.data,
                    inputText = ""
                )
            }
            is CommandResult.ItemsCleared -> {
                uiState = uiState.copy(
                    isError   = false,
                    isLoading = false,
                    items     = emptyList(),
                    inputText = ""
                )
            }
            is CommandResult.Error -> {
                uiState = uiState.copy(
                    isError      = true,
                    errorMessage = result.errorMessage.toString(),
                    isLoading    = false
                )
            }
        }
    }

    fun onEditItem(item: SaveableItem, newContent: String) {
        scope.launch {
            val result = commandHandler.handle(Command.EditItem(item.id, newContent))
            when (result) {
                is CommandResult.Success -> uiState = uiState.copy(
                    items          = uiState.items.map { if (it.id == item.id) it.copy(content = newContent) else it },
                    successMessage = "Item is updated successfully"
                )
                is CommandResult.Error -> uiState = uiState.copy(
                    isError      = true,
                    errorMessage = "Failed to edit: ${result.errorMessage}"
                )
                else -> Unit
            }
        }
    }

    fun onDeleteItem(item: SaveableItem) {
        scope.launch {
            val result = commandHandler.handle(Command.DeleteItem(item.id))
            when (result) {
                is CommandResult.Success -> uiState = uiState.copy(
                    items          = uiState.items.filter { it.id != item.id },
                    successMessage = "Item is deleted successfully"
                )
                is CommandResult.Error -> uiState = uiState.copy(
                    isError      = true,
                    errorMessage = "Failed to delete: ${result.errorMessage}"
                )
                else -> Unit
            }
        }
    }

    fun onDispose() = scope.cancel()
}
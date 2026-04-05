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
import org.simpleapps.saveable.domain.translation.TranslationResult
import org.simpleapps.saveable.domain.usecases.GetCategoriesUseCase
import org.simpleapps.saveable.util.logger
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO

data class MainUiState(
    val inputText          : String             = "",
    val categories         : List<String>       = emptyList(),
    val items              : List<SaveableItem> = emptyList(),
    val isError            : Boolean            = false,
    val errorMessage       : String             = "",
    val successMessage     : String             = "",
    val isLoading          : Boolean            = false,
    val suggestions        : List<Suggestion>   = emptyList(),
    val selectedSuggestion : Int                = -1,
    val pendingImageBase64 : String?            = null,
    val pendingImageLabel  : String             = "",
    val pendingTranslation : TranslationResult? = null
)

class MainStateHolder(
    private val commandParser        : CommandParser,
    private val commandHandler       : CommandHandler,
    private val getCategoriesUseCase : GetCategoriesUseCase
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

    fun onDismissTranslation() {
        uiState = uiState.copy(pendingTranslation = null)
    }

    fun onPasteEvent(): Boolean {
        return try {
            val clipboard    = Toolkit.getDefaultToolkit().systemClipboard
            val transferable = clipboard.getContents(null) ?: return false
            if (!transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) return false
            val image = transferable.getTransferData(DataFlavor.imageFlavor) as? BufferedImage
                ?: return false
            val baos   = ByteArrayOutputStream()
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

            val rawInput     = uiState.inputText.trim()
            val pendingImage = uiState.pendingImageBase64
            val translation  = uiState.pendingTranslation

            val command: Command? = when {

                // ── Case 1: translation card open + /add <category> ─────────────────
                translation != null && rawInput.startsWith("/add ") -> {
                    // parse with allowImageOnlyAdd=true so "/add notes" (no content) is valid
                    val parsed = commandParser.parse(rawInput, allowImageOnlyAdd = true)
                    if (parsed is Command.AddItem) {
                        val content = buildTranslationContent(translation)
                        uiState = uiState.copy(pendingTranslation = null)
                        parsed.copy(content = content)
                    } else null
                }

                // ── Case 2: image pending ────────────────────────────────────────────
                // Pass allowImageOnlyAdd=true so "/add notes" succeeds even without text content
                pendingImage != null -> {
                    val parsed = commandParser.parse(rawInput, allowImageOnlyAdd = true)
                    if (parsed is Command.AddItem) {
                        parsed.copy(imageBase64 = pendingImage)
                    } else {
                        // Not an /add command — execute normally without the image
                        parsed
                    }
                }

                // ── Case 3: normal command ───────────────────────────────────────────
                else -> commandParser.parse(rawInput)
            }

            if (command == null) {
                uiState = uiState.copy(
                    isError      = true,
                    errorMessage = "Unknown input, try again",
                    isLoading    = false
                )
                return@launch
            }

            uiState = uiState.copy(pendingImageBase64 = null, pendingImageLabel = "")
            val result = commandHandler.handle(command)
            handleCommandResult(result)
        }
    }

    private fun buildTranslationContent(t: TranslationResult): String = buildString {
        append("${t.sourceText} [${t.destinationText}]")
        if (!t.phonetic.isNullOrBlank()) append(" /${t.phonetic}/")
        appendLine()
        if (t.possibleTranslations.isNotEmpty()) {
            appendLine("Translations: ${t.possibleTranslations.joinToString(", ")}")
        }
        t.definitions.take(3).forEach { def ->
            appendLine()
            append("[${def.partOfSpeech}] ${def.definition}")
            if (!def.example.isNullOrBlank()) appendLine("\ne.g. ${def.example}")
            else appendLine()
            if (def.synonyms.isNotEmpty()) {
                appendLine("synonyms: ${def.synonyms.take(6).joinToString(", ")}")
            }
        }
    }.trimEnd()

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
            is CommandResult.TranslationData -> {
                uiState = uiState.copy(
                    isError            = false,
                    isLoading          = false,
                    pendingTranslation = result.result,
                    inputText          = ""
                )
            }
            is CommandResult.ItemsCleared -> {
                uiState = uiState.copy(
                    isError            = false,
                    isLoading          = false,
                    items              = emptyList(),
                    pendingTranslation = null,
                    inputText          = ""
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
                is CommandResult.Error   -> uiState = uiState.copy(
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
                is CommandResult.Error   -> uiState = uiState.copy(
                    isError      = true,
                    errorMessage = "Failed to delete: ${result.errorMessage}"
                )
                else -> Unit
            }
        }
    }

    fun onDispose() = scope.cancel()
}
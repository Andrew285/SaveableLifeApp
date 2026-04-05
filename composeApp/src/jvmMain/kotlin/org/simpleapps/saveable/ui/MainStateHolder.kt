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
import org.simpleapps.saveable.domain.category.Category
import org.simpleapps.saveable.domain.command.CommandHandler
import org.simpleapps.saveable.domain.command.CommandParser
import org.simpleapps.saveable.domain.command.CommandResult
import org.simpleapps.saveable.util.logger

data class MainUiState(
    val inputText: String = "",
    val categories: List<Category> = emptyList(),
    val items: List<SaveableItem> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    val isLoading: Boolean = false,
)

class MainStateHolder(
    private val commandParser  : CommandParser,
    private val commandHandler : CommandHandler
) {
    var uiState by mutableStateOf(MainUiState()); private set
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val log = logger()

    init {
        loadCategories()
    }
    fun loadCategories() {
//        categories = listOf(
//            Category(0, "books"),
//            Category(1, "passwords"),
//            Category(2, "phone_numbers")
//        )
    }

    fun onInputChanged(text: String) {
        uiState = uiState.copy(
            inputText = text
        )
    }

    fun onSubmit() {
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

            when (val result = commandHandler.handle(command)) {
                is CommandResult.Success -> {
                    log.info("Command succeeded: ${result.message}")
                    uiState = uiState.copy(
                        isError = false,
                        isLoading = false,
                        successMessage = result.message,
                    )
                }
                is CommandResult.ItemsList -> {
                    log.debug("Got ${result.data.size} items")
                    uiState = uiState.copy(
                        isError = false,
                        isLoading = false,
                        items = result.data
                    )
                }
                is CommandResult.Error -> {
                    log.error("Command failed: ${result.errorMessage}")
                    uiState = uiState.copy(
                        isError = true,
                        errorMessage = result.errorMessage.toString(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onDispose() = scope.cancel()
}
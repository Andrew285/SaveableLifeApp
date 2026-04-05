package org.simpleapps.saveable.domain.autocomplete

data class Suggestion(
    val displayText  : String,   // shown in UI: "/add notes"
    val insertText   : String,   // inserted into field: "/add notes "
    val description  : String    // hint: "Add item to notes"
)

class AutocompleteEngine {

    private val commands = listOf("/add", "/add_category", "/list", "/search", "/delete", "/pin", "/help", "/clear")

    fun suggest(input: String, categories: List<String>): List<Suggestion> {
        if (!input.startsWith("/")) return emptyList()

        val parts = input.trimStart().split(" ")
        val cmd   = parts[0]

        return when {
            // still typing the command → suggest matching commands
            parts.size == 1 -> {
                commands
                    .filter { it.startsWith(cmd) && it != cmd }
                    .map { Suggestion(it, "$it ", describeCommand(it)) }
            }

            parts.size == 2 && parts[0] == "/add_category" -> {
                emptyList()
            }

            // typed full command + space + partial category
            parts.size == 2 && cmd in listOf("/add", "/list") -> {
                val partial = parts[1]
                categories
                    .filter { it.startsWith(partial, ignoreCase = true) }
                    .map {
                        Suggestion(
                            displayText = "$cmd $it",
                            insertText  = "$cmd $it ",
                            description = describeCommand(cmd, it)
                        )
                    }
            }

            else -> emptyList()
        }
    }

    private fun describeCommand(cmd: String, category: String = "") = when (cmd) {
        "/add"    -> if (category.isEmpty()) "Add item to a category" else "Add item to $category"
        "/list"   -> if (category.isEmpty()) "List items by category" else "List all $category items"
        "/search" -> "Search across all items"
        "/delete" -> "Delete item by id"
        "/pin"    -> "Pin item by id"
        "/help"   -> "Show all commands"
        "/clear"  -> "Clear results"
        else      -> ""
    }
}
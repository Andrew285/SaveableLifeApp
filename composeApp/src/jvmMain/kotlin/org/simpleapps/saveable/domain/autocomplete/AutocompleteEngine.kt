package org.simpleapps.saveable.domain.autocomplete

data class Suggestion(
    val displayText : String,
    val insertText  : String,
    val description : String
)

class AutocompleteEngine {

    private val commands = listOf(
        "/add", "/add_category", "/list", "/search",
        "/delete", "/pin", "/translate", "/help", "/clear"
    )

    fun suggest(input: String, categories: List<String>): List<Suggestion> {
        if (!input.startsWith("/")) return emptyList()

        val parts = input.trimStart().split(" ")
        val cmd   = parts[0]

        return when {
            // Still typing the command word itself
            parts.size == 1 -> {
                commands
                    .filter { it.startsWith(cmd) && it != cmd }
                    .map { Suggestion(it, "$it ", describeCommand(it)) }
            }

            parts[0] == "/add_category" -> emptyList()

            // /add <partial-category>  or  /list <partial-category>
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

            // /translate <partial> – no category suggestions, just show usage hint
            parts.size == 1 && cmd == "/translate" -> {
                listOf(
                    Suggestion(
                        displayText = "/translate",
                        insertText  = "/translate ",
                        description = "Translate a word to Ukrainian"
                    )
                )
            }

            else -> emptyList()
        }
    }

    private fun describeCommand(cmd: String, category: String = "") = when (cmd) {
        "/add"       -> if (category.isEmpty()) "Add item to a category" else "Add item to $category"
        "/list"      -> if (category.isEmpty()) "List items by category" else "List all $category items"
        "/search"    -> "Search across all items"
        "/delete"    -> "Delete item by id"
        "/pin"       -> "Pin item by id"
        "/translate" -> "Translate a word to Ukrainian"
        "/help"      -> "Show all commands"
        "/clear"     -> "Clear results"
        else         -> ""
    }
}
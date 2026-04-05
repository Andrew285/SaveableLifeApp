package org.simpleapps.saveable.domain.command

class CommandParser {

    /**
     * Tokenises [text] respecting double-quoted groups.
     * "/add notes "hello world"" → ["/add", "notes", "hello world"]
     * "/translate "give up""     → ["/translate", "give up"]
     */
    private fun tokenize(text: String): List<String> {
        val tokens  = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (ch in text) {
            when {
                ch == '"'              -> inQuotes = !inQuotes
                ch == ' ' && !inQuotes -> {
                    if (current.isNotEmpty()) { tokens += current.toString(); current.clear() }
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) tokens += current.toString()
        return tokens
    }

    /**
     * [allowImageOnlyAdd] — when true, "/add <category>" with no content text is valid.
     * Used when an image is already pending in state.
     */
    fun parse(text: String, allowImageOnlyAdd: Boolean = false): Command? {
        val parts = tokenize(text.trim())
        if (parts.isEmpty()) return null

        return when (parts[0]) {
            "/add" -> {
                // Need at least category; content is optional when an image will be attached
                if (parts.size < 2) return null
                if (parts.size < 3 && !allowImageOnlyAdd) return null
                Command.AddItem(
                    categoryName = parts[1],
                    content      = parts.drop(2).joinToString(" ")  // "" when no content tokens
                )
            }
            "/add_category" -> {
                if (parts.size < 2) return null
                Command.AddCategory(parts[1])
            }
            "/list" -> {
                if (parts.size < 2) return null
                Command.List(parts[1])
            }
            "/translate" -> {
                if (parts.size < 2) return null
                Command.Translate(parts.drop(1).joinToString(" "))
            }
            "/clear" -> Command.Clear
            else    -> null
        }
    }
}
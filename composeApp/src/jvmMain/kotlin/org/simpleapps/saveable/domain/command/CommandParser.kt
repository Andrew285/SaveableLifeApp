package org.simpleapps.saveable.domain.command

class CommandParser {

    /**
     * Tokenises [text] respecting double-quoted groups.
     * "/add notes "hello world"" → ["/add", "notes", "hello world"]
     */
    private fun tokenize(text: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (ch in text) {
            when {
                ch == '"' -> inQuotes = !inQuotes          // toggle quote mode, don't include the char
                ch == ' ' && !inQuotes -> {                // space outside quotes = delimiter
                    if (current.isNotEmpty()) {
                        tokens += current.toString()
                        current.clear()
                    }
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) tokens += current.toString()
        return tokens
    }

    fun parse(text: String): Command? {
        val parts = tokenize(text.trim())
        if (parts.isEmpty()) return null

        return when (parts[0]) {
            "/add" -> {
                // /add <category> <content…>
                // content may be a single quoted token or everything after category
                if (parts.size < 3) return null
                val category = parts[1]
                // join remaining tokens so un-quoted multi-word content also works
                val content = parts.drop(2).joinToString(" ")
                Command.AddItem(category, content)
            }
            "/add_category" -> {
                if (parts.size < 2) return null
                Command.AddCategory(parts[1])
            }
            "/list" -> {
                if (parts.size < 2) return null
                Command.List(parts[1])
            }
            "/clear" -> Command.Clear
            else -> null
        }
    }
}
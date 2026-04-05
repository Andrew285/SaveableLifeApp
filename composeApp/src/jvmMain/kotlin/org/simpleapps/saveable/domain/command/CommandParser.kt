package org.simpleapps.saveable.domain.command

class CommandParser {
    fun parse(text: String): Command? {
        val commandComponents = text.split(" ")

        val startComponent = commandComponents[0]

        return when (startComponent) {
            "/add" -> Command.AddItem(commandComponents[1], commandComponents[2])
//            "/delete" -> Command.DeleteItem(commandComponents[1].toInt())
            "/add_category" -> Command.AddCategory(commandComponents[1])
//            "/delete_category" -> Command.DeleteCategory(commandComponents[1].toInt())
            "/list" -> Command.List(commandComponents[1])
            "/clear" -> Command.Clear
            else -> null
        }
    }
}


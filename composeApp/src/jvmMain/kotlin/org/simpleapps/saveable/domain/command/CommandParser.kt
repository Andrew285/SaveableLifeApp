package org.simpleapps.saveable.domain.command

class CommandParser {
    fun parse(text: String): Command? {
        val commandComponents = text.split(" ")

        val startComponent = commandComponents[0]

        return when (startComponent) {
            "/add" -> Command.Add(commandComponents[1], commandComponents[2])
            "/list" -> Command.List(commandComponents[1])
            "/clear" -> Command.Clear
            else -> null
        }
    }
}


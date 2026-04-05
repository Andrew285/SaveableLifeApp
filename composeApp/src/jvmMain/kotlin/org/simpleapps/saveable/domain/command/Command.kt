package org.simpleapps.saveable.domain.command

sealed class Command {
    data class Add(val categoryName: String, val content: String): Command()
    data class List(val categoryName: String): Command()
}

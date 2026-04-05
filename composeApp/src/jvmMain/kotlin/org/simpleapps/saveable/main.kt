package org.simpleapps.saveable

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.compose.KoinContext
import org.koin.core.context.KoinContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.simpleapps.saveable.di.appModule

fun main() = application {

    startKoin {
        printLogger(Level.INFO)
        modules(appModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "SaveableApp",
    ) {
        KoinContext {
            App()
        }
    }
}
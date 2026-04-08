import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation(libs.exposed.core)
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.dao)
            implementation(libs.sqlite.jdbc)
            implementation(libs.logback)

            implementation("org.json:json:20240303")
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.simpleapps.saveable.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SaveableApp"
            packageVersion = "1.0.0"

            modules(
                "java.sql",           // потрібен для JDBC/SQLite
                "java.naming",        // потрібен для Exposed
                "java.management",    // логування
                "java.instrument",    // logback
                "java.security.jgss"  // може знадобитись
            )
        }
    }
}

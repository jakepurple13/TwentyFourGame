import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    android {
        namespace = "com.programmersbox.twentyfourgame.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources {
            enable = true
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop")

    applyDefaultHierarchyTemplate()
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(libs.datastore.core)
            implementation(libs.datastore.preferences)
            implementation(libs.colorpicker.compose)
        }
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            api(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.components.ui.tooling.preview)
            implementation(libs.lifecycle.viewmodel.compose)
            /*implementation(libs.datastore.core)
            implementation(libs.datastore.preferences)*/
            implementation(libs.kotlinx.coroutines.core)
            api(libs.material.kolor)
            implementation(libs.cupertino)
            implementation(libs.cupertino.adaptive)
            implementation(libs.cupertino.native)
            implementation(libs.cupertino.icons.extended)
            implementation(libs.colorpicker.compose)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.datastore.core)
            implementation(libs.datastore.preferences)
        }

        iosMain.dependencies {
            implementation(libs.datastore.core)
            implementation(libs.datastore.preferences)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "24 Game"
            packageVersion = "1.0.0"

            fun iconFile(extension: String) =
                project.file("src/commonMain/composeResources/drawable/twentyfourlogo.$extension")
            macOS {
                iconFile.set(iconFile("icns"))
            }
            windows {
                //iconFile.set(iconFile("ico"))
                dirChooser = true
                console = true
            }
            linux {
                //iconFile.set(iconFile("png"))
            }
        }
    }
}

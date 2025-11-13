import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.gms.google.services.get().pluginId)
    apply(plugin = libs.plugins.google.firebase.crashlytics.get().pluginId)
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

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop")

    applyDefaultHierarchyTemplate()
    
    listOf(
        iosX64(),
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
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.datastore.core)
            implementation(libs.datastore.preferences)
            implementation(libs.colorpicker.compose)
            implementation(libs.androidx.material)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
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

android {
    namespace = "com.programmersbox.twentyfourgame"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.programmersbox.twentyfourgame"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }

        create("beta") {
            initWith(getByName("debug"))
            matchingFallbacks.addAll(listOf("debug", "release"))
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
        if (file("google-services.json").exists()) {
            implementation(libs.firebase.crashlytics)
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

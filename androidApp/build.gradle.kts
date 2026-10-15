plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.baselineprofile)
}

if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.gms.google.services.get().pluginId)
    apply(plugin = libs.plugins.google.firebase.crashlytics.get().pluginId)
}

android {
    namespace = "com.programmersbox.twentyfourgame"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
}

baselineProfile {
    // Write one profile to src/main so every variant (release, beta) ships it.
    mergeIntoMain = true
    // Use the startup profile to lay out the primary dex file for faster startup.
    dexLayoutOptimization = true
    // Profiles are generated explicitly (locally or in CI) and never as part of a normal build.
    automaticGenerationDuringBuild = false
}

androidComponents {
    finalizeDsl { extension ->
        // The baseline profile plugin creates these from release, which has no signing config
        // (CI signs the release artifacts afterwards). Sign them with the debug key so they
        // can be installed on the emulator that generates profiles and runs benchmarks.
        extension.buildTypes
            .matching { it.name == "benchmarkRelease" || it.name == "nonMinifiedRelease" }
            .configureEach { signingConfig = extension.signingConfigs.getByName("debug") }
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    baselineProfile(project(":baselineprofile"))
    if (file("google-services.json").exists()) {
        implementation(libs.firebase.crashlytics)
    }
}

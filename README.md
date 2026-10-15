This is a Kotlin Multiplatform project targeting Android, iOS, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/androidApp` is the Android application. It depends on `/composeApp` and holds `MainActivity`,
  the manifest, Android resources and the release (R8) configuration.

* `/baselineprofile` generates the Baseline and Startup Profiles for `/androidApp` and holds the startup
  Macrobenchmarks. Generate the profiles on the Gradle Managed Device emulator with
  `./gradlew :androidApp:generateBaselineProfile`, or on a connected device (rooted, or API 33+) by adding
  `-Pbaselineprofile.useConnectedDevices=true`. CI regenerates them before every Android release build.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
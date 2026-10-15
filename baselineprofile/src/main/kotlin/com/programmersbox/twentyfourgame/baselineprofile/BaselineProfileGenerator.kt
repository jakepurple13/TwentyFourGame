package com.programmersbox.twentyfourgame.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the baseline profile and the startup profile for the app.
 *
 * Run with `./gradlew :androidApp:generateBaselineProfile`. By default this uses the
 * `pixel6Api34` Gradle Managed Device. Add `-Pbaselineprofile.useConnectedDevices=true`
 * to use a connected device instead (rooted, or API 33+).
 *
 * The output is written to `androidApp/src/main/generated/baselineProfiles/`.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    /**
     * Cold startup up to the first interactive frame. Also feeds the startup profile, which R8
     * uses to lay out the primary dex file.
     */
    @Test
    fun startup() = rule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        dismissInstructionsIfShown()
        waitForGame()
    }

    /**
     * The core user journeys after startup: playing a round, giving up and restarting, the
     * instructions dialog and the settings sheet.
     */
    @Test
    fun gameplay() = rule.collect(
        packageName = PACKAGE_NAME,
    ) {
        pressHome()
        startActivityAndWait()
        dismissInstructionsIfShown()
        waitForGame()
        playRound()
        giveUpAndRestart()
        openAndCloseInstructions()
        openAndCloseSettings()
    }
}

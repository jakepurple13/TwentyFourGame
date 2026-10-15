package com.programmersbox.twentyfourgame.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

const val PACKAGE_NAME = "com.programmersbox.twentyfourgame"

private const val UI_TIMEOUT = 5_000L

/**
 * The instructions dialog is shown on first launch. Dismiss it so the game is interactive.
 */
fun MacrobenchmarkScope.dismissInstructionsIfShown() {
    device.wait(Until.findObject(By.text("Let's Play")), UI_TIMEOUT)?.click()
    device.wait(Until.gone(By.text("Let's Play")), UI_TIMEOUT)
}

fun MacrobenchmarkScope.waitForGame() {
    device.wait(Until.hasObject(By.res("calculator_grid")), UI_TIMEOUT)
}

/**
 * Enters a short equation using the four number buttons and the operators, then clears it.
 */
fun MacrobenchmarkScope.playRound() {
    val grid = device.wait(Until.findObject(By.res("calculator_grid")), UI_TIMEOUT) ?: return
    val numbers = grid.children.take(4)
    val operators = listOf("+", "x", "-")

    numbers.forEachIndexed { index, number ->
        number.click()
        device.waitForIdle()
        operators.getOrNull(index)?.let { op ->
            device.findObject(By.text(op))?.click()
            device.waitForIdle()
        }
    }

    // Submit the equation to exercise the expression evaluator and the result UI.
    device.findObject(By.text("="))?.click()
    device.waitForIdle()
}

/**
 * Gives up to show a solution (runs the solver), then restarts with new numbers (runs the solver
 * again until it finds a solvable set).
 */
fun MacrobenchmarkScope.giveUpAndRestart() {
    device.wait(Until.findObject(By.res("give_up_button")), UI_TIMEOUT)?.click() ?: return
    device.wait(Until.findObject(By.res("restart_button")), UI_TIMEOUT)?.click()
    device.wait(Until.hasObject(By.res("give_up_button")), UI_TIMEOUT)
}

fun MacrobenchmarkScope.openAndCloseInstructions() {
    device.findObject(By.res("info_button"))?.click() ?: return
    dismissInstructionsIfShown()
}

fun MacrobenchmarkScope.openAndCloseSettings() {
    device.findObject(By.res("settings_button"))?.click() ?: return
    device.wait(Until.hasObject(By.text("Hard Mode")), UI_TIMEOUT)
    device.waitForIdle()
    device.pressBack()
    device.wait(Until.gone(By.text("Hard Mode")), UI_TIMEOUT)
}

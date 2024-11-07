import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun buttonSize(): Modifier

@Composable
expect fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme

expect val canHaveAmoled: Boolean

expect val isMobile: Boolean

expect class Settings(
    producePath: () -> String,
) {
    fun currentNumbers(): Flow<IntArray>

    suspend fun updateCurrentNumbers(newNumbers: IntArray)

    fun hardMode(): Flow<Boolean>

    suspend fun updateHardMode(hardMode: Boolean)
}

@Composable
expect fun rememberShowInstructions(): MutableState<Boolean>

@Composable
expect fun rememberHardMode(): MutableState<Boolean>

@Composable
expect fun rememberThemeColor(): MutableState<ThemeColor>

@Composable
expect fun rememberCustomColor(): MutableState<Color>

@Composable
expect fun rememberIsAmoled(): MutableState<Boolean>

@Composable
expect fun rememberUseHaptic(): MutableState<Boolean>

expect fun Modifier.semanticSetup(): Modifier
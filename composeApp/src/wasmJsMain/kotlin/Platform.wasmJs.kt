import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.materialkolor.rememberDynamicMaterialThemeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun buttonSize(): Modifier = Modifier.sizeIn(50.dp, 50.dp)

@Composable
actual fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme {
    return rememberDynamicMaterialThemeState(Color(0xFF009DFF), isDarkMode).colorScheme
}

actual val canHaveAmoled: Boolean = false

actual val isMobile: Boolean = true

@Composable
actual fun ColorPicker(
    onColorChanged: (Color) -> Unit,
) {

}

private val hardModeFlow = MutableStateFlow(false)
private val themeFlow = MutableStateFlow(ThemeColor.Dynamic)
private val colorFlow = MutableStateFlow(Color.LightGray)

actual class Settings actual constructor(
    producePath: () -> String,
) {
    private val currentNumbersFlow = MutableStateFlow(randomNumbers())

    actual fun currentNumbers(): Flow<IntArray> = currentNumbersFlow

    actual suspend fun updateCurrentNumbers(newNumbers: IntArray) {
        currentNumbersFlow.value = newNumbers
    }

    actual fun hardMode(): Flow<Boolean> = hardModeFlow

    actual suspend fun updateHardMode(hardMode: Boolean) {
        hardModeFlow.value = hardMode
    }
}

@Composable
actual fun rememberShowInstructions(): MutableState<Boolean> = rememberPreference(
    remember { MutableStateFlow(true) }
)

@Composable
actual fun rememberHardMode(): MutableState<Boolean> = rememberPreference(hardModeFlow)

@Composable
actual fun rememberThemeColor(): MutableState<ThemeColor> = rememberPreference(themeFlow)

@Composable
actual fun rememberCustomColor(): MutableState<Color> = rememberPreference(colorFlow)

@Composable
actual fun rememberIsAmoled(): MutableState<Boolean> = rememberPreference(
    remember { MutableStateFlow(false) }
)

@Composable
actual fun rememberUseHaptic(): MutableState<Boolean> = rememberPreference(
    remember { MutableStateFlow(true) }
)

@Composable
fun <R> rememberPreference(
    defaultValue: MutableStateFlow<R>,
): MutableState<R> {
    val coroutineScope = rememberCoroutineScope()
    val state by defaultValue.collectAsState()

    return remember(state) {
        object : MutableState<R> {
            override var value: R
                get() = state
                set(value) {
                    coroutineScope.launch { defaultValue.emit(value) }
                }

            override fun component1() = value
            override fun component2(): (R) -> Unit = { value = it }
        }
    }
}
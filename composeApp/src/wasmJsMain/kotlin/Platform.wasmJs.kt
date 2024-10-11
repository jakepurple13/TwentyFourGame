import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.materialkolor.rememberDynamicMaterialThemeState
import kotlinx.browser.localStorage
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

actual class Settings actual constructor(
    producePath: () -> String,
) {
    private val currentNumbersFlow = SettingPreference(
        key = "currentNumbers",
        defaultValue = randomNumbers(),
        valueToString = { it.joinToString(",") },
        valueFromString = { it.split(",").map { it.toInt() }.toIntArray() }
    )

    actual fun currentNumbers(): Flow<IntArray> = currentNumbersFlow.asFlow()

    actual suspend fun updateCurrentNumbers(newNumbers: IntArray) {
        currentNumbersFlow.update(newNumbers)
    }

    actual fun hardMode(): Flow<Boolean> = hardModeFlow.asFlow()

    actual suspend fun updateHardMode(hardMode: Boolean) {
        hardModeFlow.update(hardMode)
    }
}

private val hardModeFlow = SettingPreference(
    key = "hardMode",
    defaultValue = false,
    valueToString = { if (it == true) "true" else "false" },
    valueFromString = { it.toBooleanStrict() }
)

private val themeFlow = SettingPreference(
    key = "theme",
    defaultValue = ThemeColor.Dynamic,
    valueToString = { it.name },
    valueFromString = { ThemeColor.valueOf(it) }
)

private val colorFlow = SettingPreference(
    key = "color",
    defaultValue = Color.LightGray,
    valueToString = { it.toArgb().toString() },
    valueFromString = { Color(it.toLong()) }
)

private val showInstructionsFlow = SettingPreference(
    key = "showInstructions",
    defaultValue = true,
    valueToString = { if (it == true) "true" else "false" },
    valueFromString = { it.toBooleanStrict() }
)

@Composable
actual fun rememberShowInstructions(): MutableState<Boolean> = showInstructionsFlow.rememberPreference()

@Composable
actual fun rememberHardMode(): MutableState<Boolean> = hardModeFlow.rememberPreference()

@Composable
actual fun rememberThemeColor(): MutableState<ThemeColor> = themeFlow.rememberPreference()

@Composable
actual fun rememberCustomColor(): MutableState<Color> = colorFlow.rememberPreference()

private val isAmoledFlow = SettingPreference(
    key = "isAmoled",
    defaultValue = false,
    valueToString = { if (it == true) "true" else "false" },
    valueFromString = { it.toBooleanStrict() }
)

@Composable
actual fun rememberIsAmoled(): MutableState<Boolean> = isAmoledFlow.rememberPreference()

private val useHapticFlow = SettingPreference(
    key = "useHaptic",
    defaultValue = true,
    valueToString = { if (it == true) "true" else "false" },
    valueFromString = { it.toBooleanStrict() }
)

@Composable
actual fun rememberUseHaptic(): MutableState<Boolean> = useHapticFlow.rememberPreference()

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

open class SettingPreference<T>(
    private val key: String,
    defaultValue: T,
    private val valueToString: (T) -> String,
    private val valueFromString: (String) -> T,
) {
    protected open var state = mutableStateOf(
        localStorage.getItem(key).let {
            runCatching { valueFromString(it!!) }.getOrElse { defaultValue }
        }
    )

    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
        return state.value
    }

    operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: T) {
        state.value = value
        localStorage.setItem(key, valueToString(value))
    }

    fun asFlow() = snapshotFlow { state.value }

    fun update(value: T) {
        state.value = value
        localStorage.setItem(key, valueToString(value))
    }

    @Composable
    fun rememberPreference(): MutableState<T> {
        return remember(state) {
            object : MutableState<T> {
                override var value: T
                    get() = state.value
                    set(value) {
                        state.value = value
                        localStorage.setItem(key, valueToString(value))
                    }

                override fun component1() = value
                override fun component2(): (T) -> Unit = { value = it }
            }
        }
    }
}
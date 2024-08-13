import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.materialkolor.rememberDynamicMaterialThemeState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import kotlin.random.Random
import kotlin.text.map

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun buttonSize(): Modifier = Modifier.sizeIn(50.dp, 50.dp)

@Composable
actual fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme {
    return rememberDynamicMaterialThemeState(Color(0xFF009DFF), isDarkMode).colorScheme
}

actual val canHaveAmoled: Boolean = false

actual val isMobile: Boolean = false

@Composable
actual fun ColorPicker(
    onColorChanged: (Color) -> Unit,
) {
    val controller = rememberColorPickerController()
    HsvColorPicker(
        onColorChanged = { colorEnvelope: ColorEnvelope ->
            onColorChanged(colorEnvelope.color)
        },
        controller = controller,
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(10.dp),
    )
}

private lateinit var dataStore: DataStore<Preferences>

actual class Settings actual constructor(
    producePath: () -> String,
) {
    init {
        if (!::dataStore.isInitialized)
            dataStore = PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
    }

    companion object {
        const val DATA_STORE_FILE_NAME = "twentyfourgame.preferences_pb"

        val INSTRUCTIONS = booleanPreferencesKey("show_instructions")

        val CURRENT_NUMBERS = stringPreferencesKey("current_numbers")

        val HARD_MODE = booleanPreferencesKey("hard_mode")

        val THEME_COLOR = stringPreferencesKey("theme_color")

        val CUSTOM_COLOR = intPreferencesKey("custom_color")

        val IS_AMOLED = booleanPreferencesKey("is_amoled")

        val USE_HAPTIC = booleanPreferencesKey("use_haptic")
    }

    private val defaultFour = IntArray(4) { Random(it).nextInt(1, 10) }

    actual fun currentNumbers() = dataStore.data.map {
        it[CURRENT_NUMBERS]
            ?.map { number -> number.toString().toInt() }
            ?.toIntArray()
            ?: defaultFour
    }

    actual suspend fun updateCurrentNumbers(
        newNumbers: IntArray,
    ) {
        dataStore.edit {
            it[CURRENT_NUMBERS] = newNumbers.joinToString("") { number -> number.toString() }
        }
    }

    actual fun hardMode() = dataStore.data.map {
        it[HARD_MODE] ?: false
    }

    actual suspend fun updateHardMode(
        hardMode: Boolean,
    ) {
        dataStore.edit { it[HARD_MODE] = hardMode }
    }
}

@Composable
actual fun rememberShowInstructions() = rememberPreference(
    key = Settings.INSTRUCTIONS,
    defaultValue = false
)

@Composable
actual fun rememberHardMode() = rememberPreference(
    key = Settings.HARD_MODE,
    defaultValue = false
)

@Composable
actual fun rememberThemeColor() = rememberPreference(
    key = Settings.THEME_COLOR,
    mapToKey = { it.name },
    mapToType = { runCatching { ThemeColor.valueOf(it) }.getOrDefault(ThemeColor.Dynamic) },
    defaultValue = ThemeColor.Dynamic
)

@Composable
actual fun rememberCustomColor() = rememberPreference(
    key = Settings.CUSTOM_COLOR,
    mapToType = { Color(it) },
    mapToKey = { it.toArgb() },
    defaultValue = Color.LightGray
)

@Composable
actual fun rememberIsAmoled() = rememberPreference(
    key = Settings.IS_AMOLED,
    defaultValue = false
)

@Composable
actual fun rememberUseHaptic() = rememberPreference(
    key = Settings.USE_HAPTIC,
    defaultValue = true
)

@Composable
fun <T> rememberPreference(
    key: Preferences.Key<T>,
    defaultValue: T,
): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()
    val state by remember(::dataStore.isInitialized) {
        if (::dataStore.isInitialized) {
            dataStore
                .data
                .mapNotNull { it[key] ?: defaultValue }
                .distinctUntilChanged()
        } else {
            emptyFlow()
        }
    }.collectAsState(defaultValue)

    return remember(state) {
        object : MutableState<T> {
            override var value: T
                get() = state
                set(value) {
                    coroutineScope.launch {
                        dataStore.edit { it[key] = value }
                    }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

@Composable
fun <T, R> rememberPreference(
    key: Preferences.Key<T>,
    mapToType: (T) -> R?,
    mapToKey: (R) -> T,
    defaultValue: R,
): MutableState<R> {
    val coroutineScope = rememberCoroutineScope()
    val state by remember(::dataStore.isInitialized) {
        if (::dataStore.isInitialized) {
            dataStore
                .data
                .mapNotNull { it[key]?.let(mapToType) ?: defaultValue }
                .distinctUntilChanged()
        } else {
            emptyFlow()
        }
    }.collectAsState(defaultValue)

    return remember(state) {
        object : MutableState<R> {
            override var value: R
                get() = state
                set(value) {
                    coroutineScope.launch {
                        dataStore.edit { it[key] = value.let(mapToKey) }
                    }
                }

            override fun component1() = value
            override fun component2(): (R) -> Unit = { value = it }
        }
    }
}
import android.os.Build
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import kotlin.random.Random

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun buttonSize(): Modifier = Modifier.aspectRatio(1f)

@Composable
actual fun colorSchemeSetup(isDarkMode: Boolean, dynamicColor: Boolean): ColorScheme {
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDarkMode -> darkColorScheme()
        else -> lightColorScheme()
    }
}

actual val canHaveAmoled: Boolean = true

actual val isMobile: Boolean = true

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
actual fun rememberShowInstructions() = rememberPreferenceSpecial(
    key = Settings.INSTRUCTIONS,
    defaultValue = true,
    defaultComposeValue = false
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
fun <T> rememberPreferenceSpecial(
    key: Preferences.Key<T>,
    defaultValue: T,
    defaultComposeValue: T,
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
    }.collectAsState(defaultComposeValue)

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
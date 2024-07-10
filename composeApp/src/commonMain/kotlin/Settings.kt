import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import kotlin.random.Random

private lateinit var dataStore: DataStore<Preferences>

class Settings(
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
    }

    private val defaultFour = IntArray(4) { Random(it).nextInt(1, 10) }

    fun currentNumbers() = dataStore.data.map {
        it[CURRENT_NUMBERS]
            ?.map { number -> number.toString().toInt() }
            ?.toIntArray()
            ?: defaultFour
    }

    suspend fun updateCurrentNumbers(
        newNumbers: IntArray,
    ) {
        dataStore.edit {
            it[CURRENT_NUMBERS] = newNumbers.joinToString("") { number -> number.toString() }
        }
    }

    fun hardMode() = dataStore.data.map {
        it[HARD_MODE] ?: false
    }

    suspend fun updateHardMode(
        hardMode: Boolean,
    ) {
        dataStore.edit { it[HARD_MODE] = hardMode }
    }
}

@Composable
fun rememberShowInstructions() = rememberPreference(
    key = Settings.INSTRUCTIONS,
    defaultValue = false
)

@Composable
fun rememberHardMode() = rememberPreference(
    key = Settings.HARD_MODE,
    defaultValue = false
)

@Composable
fun rememberThemeColor() = rememberPreference(
    key = Settings.THEME_COLOR,
    mapToKey = { it.name },
    mapToType = { runCatching { ThemeColor.valueOf(it) }.getOrDefault(ThemeColor.Dynamic) },
    defaultValue = ThemeColor.Dynamic
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.rememberDynamicMaterialThemeState
import java.util.*

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "24 Game",
    ) {
        App(
            settings = remember { Settings { Settings.DATA_STORE_FILE_NAME } }
        )
    }
}

fun main2() {
    val r = Random()
    val n = IntArray(N_CARDS)
    for (j in 0..9) {
        for (i in 0 until N_CARDS) {
            n[i] = 1 + r.nextInt(MAX_DIGIT)
            print(" ${n[i]}")
        }
        print(":  ")
        val stringBuilder = StringBuilder()
        print(if (solve24(n, stringBuilder)) "\t|\t" else "No solution")
        println(stringBuilder.toString())
    }
}